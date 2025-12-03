package com.zhongjia.subtitlefusion.service.videochainv2;

import com.zhongjia.subtitlefusion.ffmpeg.FFmpegExecutor;
import com.zhongjia.subtitlefusion.model.TaskInfo;
import com.zhongjia.subtitlefusion.model.TaskState;
import com.zhongjia.subtitlefusion.model.VideoChainV2Request;
import com.zhongjia.subtitlefusion.model.capcut.CapCutResponse;
import com.zhongjia.subtitlefusion.model.capcut.DraftRefOutput;
import com.zhongjia.subtitlefusion.model.capcut.GenerateVideoOutput;
import com.zhongjia.subtitlefusion.service.DistributedTaskManagementService;
import com.zhongjia.subtitlefusion.service.FileDownloadService;
import com.zhongjia.subtitlefusion.service.MinioService;
import com.zhongjia.subtitlefusion.service.api.CapCutApiClient;
import com.zhongjia.subtitlefusion.util.MediaIoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoChainV2AsyncService {

    private final DistributedTaskManagementService tasks;
    private final FFmpegExecutor ffmpegExecutor;
    private final FileDownloadService downloader;
    private final MinioService minioService;
    private final VideoChainV2DraftWorkflowService workflow;
    private final CapCutApiClient apiClient;

    @Async
    public void processAsync(String taskId, VideoChainV2Request request) {
        Path workDir = null;
        List<Path> tempFiles = new ArrayList<>();
        try {
            tasks.updateTaskState(taskId, TaskState.DOWNLOADING);
            workDir = Files.createTempDirectory("vcv2_" + taskId + "_");

            // 1) 段内拼接：下载小视频 → concat → 得到无声段视频
            tasks.updateTaskProgress(taskId, TaskState.PROCESSING, 15, "段内拼接开始");
            List<String> segmentUrls = new ArrayList<>();
            for (int i = 0; i < request.getSegmentList().size(); i++) {
                VideoChainV2Request.SegmentInfo seg = request.getSegmentList().get(i);

                // 若该段只有一个有效原始视频URL，则直接使用原URL，跳过下载/拼接/上传
                List<String> rawUrls = new ArrayList<>();
                if (seg.getVideoInfos() != null) {
                    for (VideoChainV2Request.VideoInfo vi : seg.getVideoInfos()) {
                        if (vi != null && vi.getVideoUrl() != null && !vi.getVideoUrl().isEmpty()) {
                            rawUrls.add(vi.getVideoUrl());
                        }
                    }
                }
                if (rawUrls.isEmpty()) {
                    throw new IllegalArgumentException("第 " + (i + 1) + " 段缺少无声小视频");
                }
                if (rawUrls.size() == 1) {
                    String directUrl = rawUrls.get(0);
                    log.info("[VideoChainV2] taskId={} 段{} 仅一段视频，直接使用原URL: {}", taskId, (i + 1), directUrl);
                    segmentUrls.add(directUrl);
                    continue;
                }

                List<Path> pieces = new ArrayList<>();
                if (seg.getVideoInfos() != null) {
                    for (VideoChainV2Request.VideoInfo vi : seg.getVideoInfos()) {
                        if (vi == null || vi.getVideoUrl() == null || vi.getVideoUrl().isEmpty()) continue;
                        Path p = downloader.downloadVideo(vi.getVideoUrl());
                        pieces.add(p);
                        tempFiles.add(p);
                    }
                }
                if (pieces.isEmpty()) {
                    throw new IllegalArgumentException("第 " + (i + 1) + " 段缺少无声小视频");
                }
                Path listFile = workDir.resolve("seg_" + i + "_concat.txt");
                MediaIoUtils.writeConcatList(listFile, pieces);
                tempFiles.add(listFile);
                Path segOut = workDir.resolve("segment_" + i + ".mp4");
                ffmpegExecutor.exec(new String[]{
                        "ffmpeg", "-y",
                        "-f", "concat", "-safe", "0",
                        "-i", listFile.toString(),
                        "-c", "copy",
                        segOut.toString()
                }, null);

                // 2) 上传公开可访问 URL
                tasks.updateTaskProgress(taskId, TaskState.UPLOADING, 35 + (i * 5), "上传段视频 " + (i + 1));
                try (FileInputStream in = new FileInputStream(segOut.toFile())) {
                    com.zhongjia.subtitlefusion.model.UploadResult ur = minioService.uploadToPublicBucket(
                            in, segOut.toFile().length(), segOut.getFileName().toString()
                    );
                    segmentUrls.add(ur.getUrl());
                }
                tempFiles.add(segOut);
            }

            // 3) 调用工作流生成草稿
            tasks.updateTaskProgress(taskId, TaskState.PROCESSING, 70, "生成草稿");
            DraftRefOutput draft = workflow.generateDraft(request, segmentUrls);
            if (draft == null || draft.getDraftId() == null || draft.getDraftId().isEmpty()) {
                log.warn("[VideoChainV2] 生成草稿失败：draftId 为空, taskId={}", taskId);
                tasks.markTaskFailed(taskId, "生成草稿失败：draftId 为空");
                return;
            }

            // 4) 提交 CapCut 云渲染任务（始终尝试云渲染）
            String draftId = draft.getDraftId();
            tasks.updateTaskProgress(taskId, TaskState.PROCESSING, 75, "提交云渲染任务");
            CapCutResponse<GenerateVideoOutput> genResp;
            try {
                genResp = apiClient.generateVideo(draftId, null, null);
            } catch (IllegalStateException e) {
                // 典型场景：license_key 未配置
                log.warn("[VideoChainV2] 云渲染任务提交失败（配置问题） taskId={}, err={}", taskId, e.getMessage());
                tasks.markTaskFailed(taskId, "云渲染任务提交失败（配置错误）: " + e.getMessage());
                return;
            } catch (Exception e) {
                log.warn("[VideoChainV2] 云渲染任务提交异常 taskId={}, err={}", taskId, e.getMessage(), e);
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
                log.warn("[VideoChainV2] 云渲染任务提交失败 taskId={}, err={}", taskId, bizError);
                tasks.markTaskFailed(taskId, msg);
                return;
            }

            // 将 cloudTaskId 存入任务，便于调用方后续查询云渲染进度
            TaskInfo ti = tasks.getTask(taskId);
            if (ti != null) {
                ti.setCloudTaskId(cloudTaskId);
            }

            tasks.updateTaskProgress(taskId, TaskState.PROCESSING, 80, "云渲染任务已提交");
            // 本地任务只负责“生成草稿并提交云渲染”，输出草稿预览 URL
            tasks.markTaskCompleted(taskId, draft.getDraftUrl());
        } catch (Exception e) {
            log.warn("[VideoChainV2] 异步处理失败 taskId={}, err={}", taskId, e.getMessage(), e);
            tasks.markTaskFailed(taskId, e.getMessage());
        } finally {
            // 清理临时文件
            if (tempFiles != null) {
                for (Path p : tempFiles) {
                    try { Files.deleteIfExists(p); } catch (Exception ignore) {}
                }
            }
            if (workDir != null) {
                try { Files.deleteIfExists(workDir); } catch (Exception ignore) {}
            }
        }
    }
}


