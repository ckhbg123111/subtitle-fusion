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
     * 同步执行：仅提交云渲染任务（generateVideo），拿到 cloudTaskId 并写回任务存储。
     * <p>
     * 注意：该方法<strong>不</strong>做轮询、不下载、不上传。
     *
     * @return cloudTaskId；提交失败时会标记任务失败并返回 null
     */
    public String submitCloudRenderSync(String taskId, String draftId, String resolution, String framerate) {
        try {
            tasks.updateTaskProgress(taskId, TaskState.PROCESSING, nextProgress(taskId, 5), "提交云渲染任务");
            String cloudTaskId = submitCloudRenderInternal(taskId, draftId, resolution, framerate);
            if (cloudTaskId == null || cloudTaskId.isEmpty()) {
                // submitCloudRenderInternal 内部已标记失败
                return null;
            }

            // 写入 cloudTaskId（必须走存储接口，兼容 Redis）
            tasks.updateTaskCloudTaskId(taskId, cloudTaskId);
            tasks.updateTaskProgress(taskId, TaskState.PROCESSING, nextProgress(taskId, 15), "云渲染任务已提交");
            return cloudTaskId;
        } catch (Exception e) {
            log.warn("[TempCloudRender] 同步提交云渲染异常 taskId={}, err={}", taskId, e.getMessage(), e);
            tasks.markTaskFailed(taskId, "云渲染任务提交异常: " + e.getMessage());
            return null;
        }
    }

    /**
     * 异步执行：提交云渲染任务 -> 轮询云侧状态 -> 下载成片并上传 MinIO -> 写回任务结果
     */
    @Async
    public void processCloudRenderAsync(String taskId, String draftId, String resolution, String framerate) {
        try {
            // 1) 提交云渲染任务（复用同步提交逻辑）
            String cloudTaskId = submitCloudRenderSync(taskId, draftId, resolution, framerate);
            if (cloudTaskId == null || cloudTaskId.isEmpty()) {
                // submitCloudRenderSync 内部已标记失败
                return;
            }

            // 2) 轮询云渲染进度
            String finalResultUrl = pollCloudTask(taskId, cloudTaskId);
            if (finalResultUrl == null) {
                // pollCloudTask 内部已标记失败
                return;
            }

            // 3) 下载云渲染结果并上传到 MinIO
            tasks.updateTaskProgress(taskId, TaskState.DOWNLOADING, nextProgress(taskId, 85), "下载云渲染结果");
            try {
                tasks.updateTaskProgress(taskId, TaskState.UPLOADING, nextProgress(taskId, 90), "上传结果到 MinIO");
                UploadResult uploadResult = transferCloudRenderResultToMinio(finalResultUrl);
                String minioUrl = uploadResult.getUrl();
                tasks.markTaskCompleted(taskId, minioUrl);
                log.info("[TempCloudRender] 任务完成 taskId={}, minioUrl={}", taskId, minioUrl);
            } catch (Exception e) {
                log.warn("[TempCloudRender] 下载或上传结果失败 taskId={}, err={}", taskId, e.getMessage(), e);
                tasks.markTaskFailed(taskId, "下载或上传云渲染结果失败: " + e.getMessage());
            }
        } catch (Exception e) {
            log.warn("[TempCloudRender] 任务执行异常 taskId={}, err={}", taskId, e.getMessage(), e);
            tasks.markTaskFailed(taskId, e.getMessage());
        }
    }

    /**
     * 转存云渲染结果：按云侧返回的原始 URL 下载成片，并上传到 MinIO 公开桶。
     * <p>
     * 注意：云渲染返回的 URL 往往包含签名参数，不能做 URI 规范化/重编码，否则可能导致 400。
     *
     * @param cloudUrl 云渲染结果直链 URL
     * @return 上传结果（包含 url/path）
     */
    public UploadResult transferCloudRenderResultToMinio(String cloudUrl) throws Exception {
        Path tempFile = null;
        try {
            tempFile = downloadCloudResultDirectly(cloudUrl);
            long size = Files.size(tempFile);
            String fileName = tempFile.getFileName().toString();
            try (FileInputStream in = new FileInputStream(tempFile.toFile())) {
                return minioService.uploadToPublicBucket(in, size, fileName);
            }
        } finally {
            if (tempFile != null) {
                fileDownloadService.cleanupTempFile(tempFile);
            }
        }
    }

    /**
     * 仅提交云渲染任务，返回 cloudTaskId；失败时标记任务失败并返回 null
     */
    private String submitCloudRenderInternal(String taskId, String draftId, String resolution, String framerate) {
        CapCutResponse<GenerateVideoOutput> genResp;
        try {
            genResp = apiClient.generateVideo(draftId, resolution, framerate);
        } catch (IllegalStateException e) {
            log.warn("[TempCloudRender] 提交云渲染失败（配置错误） taskId={}, err={}", taskId, e.getMessage());
            tasks.markTaskFailed(taskId, "云渲染任务提交失败（配置错误）: " + e.getMessage());
            return null;
        } catch (Exception e) {
            log.warn("[TempCloudRender] 提交云渲染异常 taskId={}, err={}", taskId, e.getMessage(), e);
            tasks.markTaskFailed(taskId, "云渲染任务提交异常: " + e.getMessage());
            return null;
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
            return null;
        }

        return cloudTaskId;
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
                                tasks.markTaskFailed(taskId, "云渲染成功但未返回结果地址，错误原因："+ status.getResult());
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


