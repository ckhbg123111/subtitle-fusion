package com.zhongjia.subtitlefusion.service.videochainv2;

import com.zhongjia.subtitlefusion.ffmpeg.FFmpegExecutor;
import com.zhongjia.subtitlefusion.model.TaskState;
import com.zhongjia.subtitlefusion.model.VideoChainV2Request;
import com.zhongjia.subtitlefusion.model.capcut.DraftRefOutput;
import com.zhongjia.subtitlefusion.service.DistributedTaskManagementService;
import com.zhongjia.subtitlefusion.service.FileDownloadService;
import com.zhongjia.subtitlefusion.service.MinioService;
import com.zhongjia.subtitlefusion.service.TemporaryCloudRenderService;
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
    private final TemporaryCloudRenderService temporaryCloudRenderService;

    @Async
    public void processAsync(String taskId, VideoChainV2Request request, Boolean cloudRender) {
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
                    // 兼容关键帧段：videoInfos 为空时允许使用 keyframeInfo.pictureUrl 作为主素材
                    if (seg != null
                            && seg.getKeyframeInfo() != null
                            && seg.getKeyframeInfo().getPictureUrl() != null
                            && !seg.getKeyframeInfo().getPictureUrl().isEmpty()) {
                        log.info("[VideoChainV2] taskId={} 段{} 使用关键帧图片作为主素材，跳过段内拼接", taskId, (i + 1));
                        segmentUrls.add(null); // 占位：草稿生成阶段走 addImage(image_main)
                        continue;
                    }
                    throw new IllegalArgumentException("第 " + (i + 1) + " 段缺少主素材：无声小视频或关键帧图片");
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

            // 3) 调用工作流生成草稿（仅生成草稿，不在此处耦合云渲染）
            tasks.updateTaskProgress(taskId, TaskState.PROCESSING, 70, "生成草稿");
            DraftRefOutput draft = workflow.generateDraft(request, segmentUrls);
            if (draft == null || draft.getDraftId() == null || draft.getDraftId().isEmpty()) {
                log.warn("[VideoChainV2] 生成草稿失败：draftId 为空, taskId={}", taskId);
                tasks.markTaskFailed(taskId, "生成草稿失败：draftId 为空");
                return;
            }
            // 如果拿到了草稿下载地址，写入任务，便于对外查询/同步到响应
            if (draft.getDraftUrl() != null && !draft.getDraftUrl().isEmpty()) {
                tasks.updateTaskDraftUrl(taskId, draft.getDraftUrl());
            }
            // 阶段性完成：草稿已生成，但整个任务最终输出为视频成片，此处仅更新进度和描述，不标记为最终完成
            if(!cloudRender){
                tasks.updateTaskProgress(taskId, TaskState.COMPLETED, 100, "CapCut 草稿已生成，请求不需要云渲染");
            }else{
                // 仅同步提交云渲染任务：提交完成即返回（不轮询、不下载、不上传）
                String cloudTaskId = temporaryCloudRenderService.submitCloudRenderSync(taskId, draft.getDraftId(), null, null);
                if (cloudTaskId == null || cloudTaskId.isEmpty()) {
                    // submitCloudRenderSync 内部已标记失败
                    return;
                }
                tasks.updateTaskProgress(taskId, TaskState.COMPLETED, 100, "CapCut 草稿已生成，云渲染任务已提交，可通过 cloudTaskId 查询进度");
            }
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


