package com.zhongjia.subtitlefusion.service;

import com.zhongjia.subtitlefusion.model.CapCutGenResponse;
import com.zhongjia.subtitlefusion.model.CapCutCloudTaskStatus;
import com.zhongjia.subtitlefusion.model.SubtitleFusionV2Request;
import com.zhongjia.subtitlefusion.model.SubtitleInfo;
import com.zhongjia.subtitlefusion.model.capcut.CapCutResponse;
import com.zhongjia.subtitlefusion.model.capcut.DraftRefOutput;
import com.zhongjia.subtitlefusion.model.capcut.GenerateVideoOutput;
import com.zhongjia.subtitlefusion.model.clip.PictureClip;
import com.zhongjia.subtitlefusion.service.api.CapCutApiClient;
import com.zhongjia.subtitlefusion.util.MediaProbeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
@Slf4j
public class DraftWorkflowService {

    private final CapCutApiClient apiClient;
    private final SubtitleService subtitleService;
    private final PictureService pictureService;
    private final FileDownloadService fileDownloadService;

    public CapCutGenResponse generateDraft(SubtitleFusionV2Request request) {
        CapCutGenResponse resp = new CapCutGenResponse();
        log.info("[workflow] 收到请求:{}", request);
        try {
            String err = validateRequest(request);
            if (err != null) {
                log.warn("[workflow] 非法请求: {}", err);
                resp.setSuccess(false);
                resp.setMessage(err);
                return resp;
            }

            int[] resolvedWh = resolveVideoResolution(request.getVideoUrl());
            int width = resolvedWh[0];
            int height = resolvedWh[1];

            CapCutResponse<DraftRefOutput> draftResp = apiClient.createDraft(width, height);
            String draftId = (draftResp != null && draftResp.getOutput() != null) ? draftResp.getOutput().getDraftId() : null;
            String draftLinkUrl = (draftResp != null && draftResp.getOutput() != null) ? draftResp.getOutput().getDraftUrl() : null;
            if (draftResp == null || !draftResp.isSuccess() || draftId == null || draftId.isEmpty()) {
                resp.setSuccess(false);
                resp.setMessage("创建草稿失败");
                return resp;
            }
            resp.setDraftId(draftId);
            log.info("[workflow] 草稿创建成功 draftId={}", draftId);

            apiClient.addVideo(draftId, request.getVideoUrl(), 0, 0, "video_main", 1.0);


			subtitleService.processSubtitles(draftId, request.getSubtitleInfo(), width, height);

            String imageIntro = "展开";
            String imageOutro = "渐隐";
//            String imageOutro = apiClient.getRandomImageOutro(null);
            List<PictureClip> pictureClips = new ArrayList<>();
            if (request.getSubtitleInfo() != null && request.getSubtitleInfo().getPictureInfoList() != null) {
                for (SubtitleInfo.PictureInfo pi : request.getSubtitleInfo().getPictureInfoList()) {
                    if (pi == null) continue;
                    PictureClip clip = new PictureClip();
                    clip.setPictureUrl(pi.getPictureUrl());
                    clip.setStartTime(pi.getStartTime());
                    clip.setEndTime(pi.getEndTime());
                    pictureClips.add(clip);
                }
            }
            pictureService.processPictures(draftId, pictureClips, imageIntro, imageOutro, width, height);

//            String draftUrl = apiClient.saveDraft(draftId);

            // 是否走云渲染
            boolean useCloud = Boolean.TRUE.equals(request.getCloudRendering());
            resp.setCloudRendering(useCloud);
            if (useCloud) {
                return submitCloudRenderingTask(draftId, draftLinkUrl, resp);
            } else {
                resp.setSuccess(true);
                resp.setDraftUrl(draftLinkUrl);
                resp.setMessage("OK");
                log.info("[workflow] 处理完成 draftId={}, draftUrl={},链接十分钟内有效", draftId, draftLinkUrl);
                return resp;
            }
        } catch (Exception e) {
            log.error("[workflow] 失败: {}", e.getMessage(), e);
            resp.setSuccess(false);
            resp.setMessage(e.getMessage());
            return resp;
        }
    }

    private String validateRequest(SubtitleFusionV2Request req) {
        if (req == null) return "请求体不能为空";
        if (req.getVideoUrl() == null || req.getVideoUrl().isEmpty()) return "videoUrl 不能为空";
        return null;
    }

    /**
     * 解析视频分辨率，失败回退 1920x1080。
     */
    private int[] resolveVideoResolution(String videoUrl) {
        int width = 1920;
        int height = 1080;
        Path tmpVideo = null;
        try {
            tmpVideo = fileDownloadService.downloadVideo(videoUrl);
            int[] wh = MediaProbeUtils.probeVideoResolution(tmpVideo);
            if (wh != null && wh.length == 2 && wh[0] > 0 && wh[1] > 0) {
                width = wh[0];
                height = wh[1];
                log.info("[workflow] 探测到视频分辨率: {}x{}", width, height);
            } else {
                log.warn("[workflow] 视频分辨率探测结果无效，使用默认 1920x1080");
            }
        } catch (Exception e) {
            log.warn("[workflow] 视频分辨率探测失败，使用默认 1920x1080: {}", e.getMessage());
        } finally {
            if (tmpVideo != null) {
                fileDownloadService.cleanupTempFile(tmpVideo);
            }
        }
        return new int[]{width, height};
    }

    private CapCutGenResponse submitCloudRenderingTask(String draftId, String draftUrl, CapCutGenResponse resp) {
        CapCutResponse<GenerateVideoOutput> genResp =
                apiClient.generateVideo(draftId, null, null);
        String taskId = null;
        boolean bizSuccess = false;
        String bizError = null;
        if (genResp != null) {
            if (!genResp.isSuccess()) {
                bizError = genResp.getError();
            }else if (genResp.getOutput() != null) {
                taskId = genResp.getOutput().getTaskId();
                bizSuccess = genResp.getOutput().isSuccess();
                bizError = genResp.getOutput().getError();
            }
        }
        if (!bizSuccess || taskId == null || taskId.isEmpty()) {
            resp.setSuccess(false);
            resp.setDraftUrl(draftUrl);
            resp.setMessage(bizError != null ? ("云渲染任务提交失败: " + bizError) : "云渲染任务提交失败");
            log.warn("[workflow] 云渲染任务提交失败 draftId={}, err={}", draftId, bizError);
        } else {
            resp.setTaskId(taskId);
            resp.setSuccess(true);
            resp.setDraftUrl(draftUrl);
            resp.setMessage("Cloud rendering task submitted");
            log.info("[workflow] 云渲染任务已提交 draftId={}, taskId={}", draftId, taskId);
        }
        return resp;
    }
}


