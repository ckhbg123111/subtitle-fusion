package com.zhongjia.subtitlefusion.service;

import com.zhongjia.subtitlefusion.model.CapCutGenResponse;
import com.zhongjia.subtitlefusion.model.SubtitleFusionV2Request;
import com.zhongjia.subtitlefusion.service.api.CapCutApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

            String draftId = apiClient.createDraft(1080, 1920);
            if (draftId == null || draftId.isEmpty()) {
                resp.setSuccess(false);
                resp.setMessage("创建草稿失败");
                return resp;
            }
            resp.setDraftId(draftId);
            log.info("[workflow] 草稿创建成功 draftId={}", draftId);

            apiClient.addVideo(draftId, request.getVideoUrl(), 0, 0, "video_main", 1.0);

            String textIntro = apiClient.getRandomTextIntro();
            String textOutro = apiClient.getRandomTextOutro();
            subtitleService.processSubtitles(draftId, request, textIntro, textOutro);

            String imageIntro = apiClient.getRandomImageIntro(textIntro);
            String imageOutro = apiClient.getRandomImageOutro(textOutro);
            pictureService.processPictures(draftId, request, imageIntro, imageOutro);

            String draftUrl = apiClient.saveDraft(draftId);
            resp.setSuccess(true);
            resp.setDraftUrl(draftUrl);
            resp.setMessage("OK");
            log.info("[workflow] 处理完成 draftId={}, draftUrl={}", draftId, draftUrl);
            return resp;
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
}


