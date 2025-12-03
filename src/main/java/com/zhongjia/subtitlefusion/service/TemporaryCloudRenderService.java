package com.zhongjia.subtitlefusion.service;

import com.zhongjia.subtitlefusion.model.CapCutCloudResponse;
import com.zhongjia.subtitlefusion.model.CapCutCloudTaskStatus;
import com.zhongjia.subtitlefusion.model.TaskInfo;
import com.zhongjia.subtitlefusion.model.TaskState;
import com.zhongjia.subtitlefusion.model.UploadResult;
import com.zhongjia.subtitlefusion.model.capcut.CapCutResponse;
import com.zhongjia.subtitlefusion.model.capcut.GenerateVideoOutput;
import com.zhongjia.subtitlefusion.service.api.CapCutApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemporaryCloudRenderService {

    private final DistributedTaskManagementService tasks;
    private final CapCutApiClient apiClient;
    private final FileDownloadService fileDownloadService;
    private final MinioService minioService;

    /**
     * 异步执行：提交云渲染任务 -> 轮询云侧状态 -> 下载成片并上传 MinIO -> 写回任务结果
     */
    @Async
    public void processCloudRenderAsync(String taskId, String draftId, String resolution, String framerate) {
        try {
            // 1) 提交云渲染任务
            tasks.updateTaskProgress(taskId, TaskState.PROCESSING, nextProgress(taskId, 5), "提交云渲染任务");
            CapCutResponse<GenerateVideoOutput> genResp;
            try {
                genResp = apiClient.generateVideo(draftId, resolution, framerate);
            } catch (IllegalStateException e) {
                log.warn("[TempCloudRender] 提交云渲染失败（配置错误） taskId={}, err={}", taskId, e.getMessage());
                tasks.markTaskFailed(taskId, "云渲染任务提交失败（配置错误）: " + e.getMessage());
                return;
            } catch (Exception e) {
                log.warn("[TempCloudRender] 提交云渲染异常 taskId={}, err={}", taskId, e.getMessage(), e);
                tasks.markTaskFailed(taskId, "云渲染任务提交异常: " + e.getMessage());
                return;
            }

            boolean bizSuccess = false;
            String cloudTaskId = null;
            String bizError = null;
            if (genResp != null) {
                if (!genResp.isSuccess()) {
                    bizError = genResp.getError();
                } else if (genResp.getOutput() != null) {
                    cloudTaskId = genResp.getOutput().getTaskId();
                    bizSuccess = genResp.getOutput().isSuccess();
                    bizError = genResp.getOutput().getError();
                }
            }

            if (!bizSuccess || cloudTaskId == null || cloudTaskId.isEmpty()) {
                String msg = bizError != null ? ("云渲染任务提交失败: " + bizError) : "云渲染任务提交失败";
                log.warn("[TempCloudRender] 云渲染任务提交失败 taskId={}, err={}", taskId, bizError);
                tasks.markTaskFailed(taskId, msg);
                return;
            }

            // 写入 cloudTaskId，供调用方需要时查看
            TaskInfo taskInfo = tasks.getTask(taskId);
            if (taskInfo != null) {
                taskInfo.setCloudTaskId(cloudTaskId);
            }

            tasks.updateTaskProgress(taskId, TaskState.PROCESSING, nextProgress(taskId, 15), "云渲染任务已提交");

            // 2) 轮询云渲染进度
            String finalResultUrl = pollCloudTask(taskId, cloudTaskId);
            if (finalResultUrl == null) {
                // pollCloudTask 内部已标记失败
                return;
            }

            // 3) 下载云渲染结果并上传到 MinIO
            tasks.updateTaskProgress(taskId, TaskState.DOWNLOADING, nextProgress(taskId, 85), "下载云渲染结果");
            Path tempFile = null;
            try {
                // 注意：云渲染返回的 URL 已包含完整签名，不能再做 URI 规范化，否则会导致 400，这里直接按原始 URL 下载
                tempFile = downloadCloudResultDirectly(finalResultUrl);
                long size = Files.size(tempFile);
                String fileName = tempFile.getFileName().toString();
                tasks.updateTaskProgress(taskId, TaskState.UPLOADING, nextProgress(taskId, 90), "上传结果到 MinIO");
                try (FileInputStream in = new FileInputStream(tempFile.toFile())) {
                    UploadResult uploadResult = minioService.uploadToPublicBucket(in, size, fileName);
                    String minioUrl = uploadResult.getUrl();
                    tasks.markTaskCompleted(taskId, minioUrl);
                    log.info("[TempCloudRender] 任务完成 taskId={}, minioUrl={}", taskId, minioUrl);
                }
            } catch (Exception e) {
                log.warn("[TempCloudRender] 下载或上传结果失败 taskId={}, err={}", taskId, e.getMessage(), e);
                tasks.markTaskFailed(taskId, "下载或上传云渲染结果失败: " + e.getMessage());
            } finally {
                if (tempFile != null) {
                    fileDownloadService.cleanupTempFile(tempFile);
                }
            }
        } catch (Exception e) {
            log.warn("[TempCloudRender] 任务执行异常 taskId={}, err={}", taskId, e.getMessage(), e);
            tasks.markTaskFailed(taskId, e.getMessage());
        }
    }

    /**
     * 轮询云渲染任务状态，返回成功时的 result URL；失败或超时返回 null 并标记任务失败
     */
    private String pollCloudTask(String taskId, String cloudTaskId) {
        final int maxAttempts = 360; // 例如最多轮询 ~30 分钟（5s * 360）
        final long sleepMillis = 5000L;

        for (int i = 0; i < maxAttempts; i++) {
            try {
                CapCutCloudResponse<CapCutCloudTaskStatus> resp = apiClient.taskStatus(cloudTaskId);
                if (resp == null) {
                    log.warn("[TempCloudRender] 轮询云渲染状态返回空 resp, taskId={}, cloudTaskId={}", taskId, cloudTaskId);
                } else if (!Boolean.TRUE.equals(resp.getSuccess())) {
                    // 云侧接口调用失败，可能是临时问题，记录日志后继续下一轮
                    log.warn("[TempCloudRender] 轮询云渲染状态失败 taskId={}, cloudTaskId={}, err={}", taskId, cloudTaskId, resp.getError());
                } else {
                    CapCutCloudTaskStatus status = resp.getOutput();
                    if (status != null) {
                        // 同步进度/消息到任务
                        Integer progress = status.getProgress();
                        String message = status.getMessage();
                        int mappedProgress = progress != null ? Math.min(80, Math.max(20, progress)) : 20;
                        tasks.updateTaskProgress(taskId, TaskState.PROCESSING, nextProgress(taskId, mappedProgress), message != null ? message : "云渲染中");

                        String stat = status.getStatus();
                        if ("SUCCESS".equalsIgnoreCase(stat) && status.isSuccess()) {
                            String resultUrl = status.getResult();
                            if (resultUrl == null || resultUrl.isEmpty()) {
                                log.warn("[TempCloudRender] 云渲染成功但未返回 result URL taskId={}, cloudTaskId={}", taskId, cloudTaskId);
                                tasks.markTaskFailed(taskId, "云渲染成功但未返回结果地址");
                                return null;
                            }
                            log.info("[TempCloudRender] 云渲染成功 taskId={}, cloudTaskId={}, result={}", taskId, cloudTaskId, resultUrl);
                            return resultUrl;
                        }
                        if ("FAILURE".equalsIgnoreCase(stat) || (!status.isSuccess() && "FAILURE".equalsIgnoreCase(stat))) {
                            String err = status.getError() != null ? status.getError() : "云渲染失败";
                            log.warn("[TempCloudRender] 云渲染失败 taskId={}, cloudTaskId={}, err={}", taskId, cloudTaskId, err);
                            tasks.markTaskFailed(taskId, "云渲染失败: " + err);
                            return null;
                        }
                    }
                }

                Thread.sleep(sleepMillis);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                tasks.markTaskFailed(taskId, "云渲染轮询被中断");
                return null;
            } catch (Exception e) {
                log.warn("[TempCloudRender] 轮询云渲染状态异常 taskId={}, cloudTaskId={}, err={}", taskId, cloudTaskId, e.getMessage(), e);
                // 临时异常：继续下一轮
                try {
                    Thread.sleep(sleepMillis);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    tasks.markTaskFailed(taskId, "云渲染轮询被中断");
                    return null;
                }
            }
        }

        tasks.markTaskFailed(taskId, "云渲染任务超时");
        return null;
    }

    /**
     * 进度不回退：保证新的进度不会小于当前进度
     */
    private int nextProgress(String taskId, int target) {
        try {
            TaskInfo info = tasks.getTask(taskId);
            int current = (info != null) ? info.getProgress() : 0;
            return Math.max(current, target);
        } catch (Exception ignore) {
            return target;
        }
    }

    /**
     * 直接按云渲染返回的 URL 下载结果视频（不做 URI 规范化，避免破坏签名）
     */
    private Path downloadCloudResultDirectly(String fileUrl) throws IOException {
        log.info("[TempCloudRender] 按原始 URL 下载云渲染结果: {}", fileUrl);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String extension = extractExtensionFallback(fileUrl, ".mp4");
        String tempFileName = "cloud_result_" + timestamp + "_" + System.currentTimeMillis() + extension;
        Path tempFile = Paths.get(System.getProperty("java.io.tmpdir"), tempFileName);

        HttpURLConnection connection = (HttpURLConnection) new URL(fileUrl).openConnection();
        connection.setInstanceFollowRedirects(true);
        connection.setConnectTimeout(30_000);
        connection.setReadTimeout(60_000);

        try {
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("下载失败，HTTP响应码: " + responseCode + ", URL: " + fileUrl);
            }

            try (InputStream inputStream = connection.getInputStream()) {
                Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("[TempCloudRender] 云渲染结果下载完成: {}", tempFile);
            return tempFile;
        } finally {
            connection.disconnect();
        }
    }

    /**
     * 从 URL 中简单提取扩展名，失败时使用默认值
     */
    private String extractExtensionFallback(String url, String defaultExtension) {
        try {
            String path = url.split("\\?")[0];
            int lastDot = path.lastIndexOf('.');
            if (lastDot > 0 && lastDot < path.length() - 1) {
                String ext = path.substring(lastDot).toLowerCase();
                if (ext.length() <= 5 && ext.matches("\\.[a-z0-9]+")) {
                    return ext;
                }
            }
        } catch (Exception ignore) {
        }
        return defaultExtension;
    }
}


