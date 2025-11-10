package com.zhongjia.subtitlefusion.service;

import com.zhongjia.subtitlefusion.model.CapCutGenResponse;
import com.zhongjia.subtitlefusion.model.CapCutCloudTaskStatus;
import com.zhongjia.subtitlefusion.model.SubtitleFusionV2Request;
import com.zhongjia.subtitlefusion.model.capcut.CapCutResponse;
import com.zhongjia.subtitlefusion.model.capcut.DraftRefOutput;
import com.zhongjia.subtitlefusion.model.clip.PictureClip;
import com.zhongjia.subtitlefusion.service.api.CapCutApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DraftWorkflowService {

    private final CapCutApiClient apiClient;
    private final SubtitleService subtitleService;
    private final PictureService pictureService;

    public CapCutGenResponse generateDraft(SubtitleFusionV2Request request) {
        CapCutGenResponse resp = new CapCutGenResponse();
        log.info("[workflow] 收到请求");
        try {
            String err = validateRequest(request);
            if (err != null) {
                log.warn("[workflow] 非法请求: {}", err);
                resp.setSuccess(false);
                resp.setMessage(err);
                return resp;
            }

            CapCutResponse<DraftRefOutput> draftResp = apiClient.createDraft(1080, 1920);
            String draftId = (draftResp != null && draftResp.getOutput() != null) ? draftResp.getOutput().getDraftId() : null;
            if (draftResp == null || !draftResp.isSuccess() || draftId == null || draftId.isEmpty()) {
                resp.setSuccess(false);
                resp.setMessage("创建草稿失败");
                return resp;
            }
            resp.setDraftId(draftId);
            log.info("[workflow] 草稿创建成功 draftId={}", draftId);

            apiClient.addVideo(draftId, request.getVideoUrl(), 0, 0, "video_main", 1.0);


            subtitleService.processSubtitles(draftId, request, null, null);

            String imageIntro = apiClient.getRandomImageIntro(null);
            String imageOutro = apiClient.getRandomImageOutro(null);
            List<PictureClip> pictureClips = new ArrayList<>();
            if (request.getSubtitleInfo() != null && request.getSubtitleInfo().getPictureInfoList() != null) {
                for (SubtitleFusionV2Request.PictureInfo pi : request.getSubtitleInfo().getPictureInfoList()) {
                    if (pi == null) continue;
                    PictureClip clip = new PictureClip();
                    clip.setPictureUrl(pi.getPictureUrl());
                    clip.setStartTime(pi.getStartTime());
                    clip.setEndTime(pi.getEndTime());
                    pictureClips.add(clip);
                }
            }
            pictureService.processPictures(draftId, pictureClips, imageIntro, imageOutro);

            String draftUrl = apiClient.saveDraft(draftId);

            // 是否走云渲染
            boolean useCloud = Boolean.TRUE.equals(request.getCloudRendering());
            resp.setCloudRendering(useCloud);
            if (useCloud) {
                String taskId = apiClient.generateVideo(draftId, null, null);
                resp.setTaskId(taskId);
                resp.setSuccess(true);
                resp.setDraftUrl(draftUrl);
                resp.setMessage("Cloud rendering task submitted");
                log.info("[workflow] 云渲染任务已提交 draftId={}, taskId={}", draftId, taskId);
                return resp;
            } else {
                resp.setSuccess(true);
                resp.setDraftUrl(draftUrl);
                resp.setMessage("OK");
                log.info("[workflow] 处理完成 draftId={}, draftUrl={}", draftId, draftUrl);
                return resp;
            }
        } catch (Exception e) {
            log.error("[workflow] 失败: {}", e.getMessage(), e);
            resp.setSuccess(false);
            resp.setMessage(e.getMessage());
            return resp;
        }
    }

    public CapCutCloudTaskStatus cloudTaskStatus(String taskId) {
        return apiClient.taskStatus(taskId);
    }

    private String validateRequest(SubtitleFusionV2Request req) {
        if (req == null) return "请求体不能为空";
        if (req.getVideoUrl() == null || req.getVideoUrl().isEmpty()) return "videoUrl 不能为空";
        return null;
    }
}


