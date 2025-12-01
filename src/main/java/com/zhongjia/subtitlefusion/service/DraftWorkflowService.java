package com.zhongjia.subtitlefusion.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhongjia.subtitlefusion.model.*;
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
    private final ObjectMapper objectMapper;

    public CapCutGenResponse generateDraft(SubtitleFusionV2Request request) {
        CapCutGenResponse resp = new CapCutGenResponse();
        String reqJson;
        try {
            reqJson = objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            reqJson = String.valueOf(request);
        }
        log.info("[workflow] 收到请求:{}", reqJson);
        try {
            String err = validateRequest(request);
            if (err != null) {
                log.warn("[workflow] 非法请求: {}", err);
                resp.setSuccess(false);
                resp.setMessage(err);
                return resp;
            }

            int width;
            int height;
            Integer reqW = request.getVideoWidth();
            Integer reqH = request.getVideoHeight();
            if (reqW != null && reqW > 0 && reqH != null && reqH > 0) {
                width = reqW;
                height = reqH;
                log.info("[workflow] 使用请求提供的分辨率: {}x{}", width, height);
            } else {
                int[] resolvedWh = resolveVideoResolution(request.getVideoUrl());
                width = resolvedWh[0];
                height = resolvedWh[1];
            }

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

            // 添加左上角文字水印：“AI生成”
            try {
                Double videoDuration = apiClient.getDuration(request.getVideoUrl());
                double wmStart = 0.0;
                double wmEnd = (videoDuration != null && videoDuration > 0) ? videoDuration : 600.0; // 无法获取时兜底10分钟
                java.util.Map<String, Object> wm = new java.util.HashMap<>();
                wm.put("text", "AI生成");
                wm.put("start", wmStart);
                wm.put("end", wmEnd);
                wm.put("draft_id", draftId);
                // 位置：左上角贴边（使用相对位移，坐标原点为画布中心，-0.5/-0.5 贴边）
                int marginPx = 6;
                double tx = -0.85;// + (marginPx / (double) Math.max(1, width));
                double ty = 0.95;// - (marginPx / (double) Math.max(1, height));
                wm.put("transform_x", tx);
                wm.put("transform_y", ty);
                // 视觉：半透明白字 + 黑色描边，较小字号；左对齐，置顶图层
                wm.put("font_color", "#FFFFFF");
                wm.put("font_alpha", 0.65);
                wm.put("border_color", "#000000");
                wm.put("border_alpha", 0.6);
                wm.put("border_width", 2);
                wm.put("font_size", 8);
                wm.put("align", 0); // 左对齐
                wm.put("track_name", "watermark_text");
                wm.put("relative_index", 999); // 尽量置顶
                wm.put("width", width);
                wm.put("height", height);
                apiClient.addText(wm);
            } catch (Exception e) {
                log.warn("[workflow] 添加文字水印失败（忽略不中断）: {}", e.getMessage());
            }

            String imageIntro = null;
            String imageOutro = null;
//            String imageOutro = apiClient.getRandomImageOutro(null);
            List<PictureClip> pictureClips = new ArrayList<>();
            if (request.getPictureInfoList() != null) {
                for (PictureInfo pi : request.getPictureInfoList()) {
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


