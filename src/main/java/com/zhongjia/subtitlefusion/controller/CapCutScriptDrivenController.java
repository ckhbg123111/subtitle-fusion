package com.zhongjia.subtitlefusion.controller;

import com.zhongjia.subtitlefusion.model.SubtitleFusionV2Request;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Map;
import java.net.URI;
import java.net.URL;

@RestController
@RequestMapping("/api/capcut-script-driven")
@Slf4j
public class CapCutScriptDrivenController {

   
    


    // CapCutAPI 基础地址（用户已提供的已部署服务）
    private static final String CAPCUT_API_BASE = "http://127.0.0.1:9003";
    // CapCutAPI 路径常量
    private static final String PATH_CREATE_DRAFT = "/create_draft";
    private static final String PATH_ADD_VIDEO = "/add_video";
    // 预留：暂未使用
    // private static final String PATH_ADD_AUDIO = "/add_audio";
    private static final String PATH_ADD_TEXT = "/add_text";
    // 预留：暂未使用
    // private static final String PATH_ADD_SUBTITLE = "/add_subtitle";
    private static final String PATH_ADD_IMAGE = "/add_image";
    private static final String PATH_SAVE_DRAFT = "/save_draft";
    private static final String PATH_GET_TEXT_INTRO_TYPES = "/get_text_intro_types";
    private static final String PATH_GET_TEXT_OUTRO_TYPES = "/get_text_outro_types";
    private static final String PATH_GET_INTRO_ANIMATION_TYPES = "/get_intro_animation_types";
    private static final String PATH_GET_OUTRO_ANIMATION_TYPES = "/get_outro_animation_types";
   

    private final RestTemplate restTemplate = new RestTemplate();

    
    @PostMapping(value = "/capcut-gen", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public CapCutGenResponse submit(@RequestBody SubtitleFusionV2Request request)  {
        CapCutGenResponse resp = new CapCutGenResponse();
        log.info("[capcut-gen] 收到请求");
        try {
            String err = validateRequest(request);
            if (err != null) {
                log.warn("[capcut-gen] 非法请求: {}", err);
                resp.setSuccess(false);
                resp.setMessage(err);
                return resp;
            }

            String draftId = createDraft();
            if (draftId == null || draftId.isEmpty()) {
                resp.setSuccess(false);
                resp.setMessage("创建草稿失败");
                return resp;
            }
            resp.setDraftId(draftId);
            log.info("[capcut-gen] 草稿创建成功 draftId={}", draftId);

            addMainVideo(draftId, request.getVideoUrl());

        String textIntro = getRandomTextIntro();
        String textOutro = getRandomTextOutro();
            processSubtitles(draftId, request, textIntro, textOutro);

        String imageIntro = getRandomImageIntro(textIntro);
        String imageOutro = getRandomImageOutro(textOutro);
            processPictures(draftId, request, imageIntro, imageOutro);

            String draftUrl = saveDraft(draftId);
            resp.setSuccess(true);
            resp.setDraftUrl(draftUrl);
            resp.setMessage("OK");
            log.info("[capcut-gen] 处理完成 draftId={}, draftUrl={}", draftId, draftUrl);
            return resp;
        } catch (Exception e) {
            log.error("[capcut-gen] 失败: {}", e.getMessage(), e);
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

    private String createDraft() {
        log.info("[capcut-gen] 创建草稿...");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        java.util.Map<String, Object> draftParams = new java.util.HashMap<>();
        draftParams.put("width", 1080);
        draftParams.put("height", 1920);
        ResponseEntity<Map<String, Object>> draftRes = restTemplate.exchange(
                CAPCUT_API_BASE + PATH_CREATE_DRAFT,
                HttpMethod.POST,
                new HttpEntity<>(draftParams, headers),
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        String draftId = extractString(draftRes.getBody(), "output", "draft_id");
        log.info("[capcut-gen] 创建草稿结果 draftId={}", draftId);
        return draftId;
    }

    private void addMainVideo(String draftId, String videoUrl) {
        log.info("[capcut-gen] 添加主视频... videoUrl={}", videoUrl);
        String encodedUrl = encodeUrl(videoUrl);
        java.util.Map<String, Object> addVideo = new java.util.HashMap<>();
        addVideo.put("draft_id", draftId);
        addVideo.put("video_url", encodedUrl);
        addVideo.put("start", 0);
        addVideo.put("end", 0);
        addVideo.put("track_name", "video_main");
        addVideo.put("volume", 1.0);
        postJson(CAPCUT_API_BASE + PATH_ADD_VIDEO, addVideo);
    }

    private void processSubtitles(String draftId, SubtitleFusionV2Request request, String textIntro, String textOutro) {
        if (request.getSubtitleInfo() == null || request.getSubtitleInfo().getCommonSubtitleInfoList() == null) return;
        log.info("[capcut-gen] 处理字幕条数: {}", request.getSubtitleInfo().getCommonSubtitleInfoList().size());
        for (SubtitleFusionV2Request.CommonSubtitleInfo si : request.getSubtitleInfo().getCommonSubtitleInfoList()) {
            if (si == null || si.getText() == null || si.getText().isEmpty()) continue;
            double start = parseToSeconds(si.getStartTime());
            double end = parseToSeconds(si.getEndTime());
            if (end <= start) end = start + 1.0;
            java.util.Map<String, Object> addText = new java.util.HashMap<>();
            addText.put("draft_id", draftId);
            addText.put("text", si.getText());
            addText.put("start", start);
            addText.put("end", end);
            addText.put("track_name", "text_fx");
            addText.put("font", "思源黑体");
            addText.put("font_color", "#FFFFFF");
            addText.put("font_size", 6.0);
            addText.put("border_width", 0.6);
            addText.put("border_color", "#000000");
            addText.put("shadow_enabled", true);
            addText.put("shadow_alpha", 0.8);
            addText.put("transform_y", -0.8);
            addText.put("intro_animation", textIntro != null ? textIntro : "Throw_Out");
            addText.put("intro_duration", 0.5);
            addText.put("outro_animation", textOutro != null ? textOutro : "Fade_Out");
            addText.put("outro_duration", 0.5);
            postJson(CAPCUT_API_BASE + PATH_ADD_TEXT, addText);

            if (si.getSubtitleEffectInfo() != null && si.getSubtitleEffectInfo().getKeyWords() != null) {
                for (String kw : si.getSubtitleEffectInfo().getKeyWords()) {
                    if (kw == null || kw.isEmpty()) continue;
                    java.util.Map<String, Object> fancy = new java.util.HashMap<>();
                    fancy.put("draft_id", draftId);
                    fancy.put("text", kw);
                    fancy.put("start", start);
                    fancy.put("end", end);
                    fancy.put("track_name", "text_fx");
                    fancy.put("font", "文轩体");
                    fancy.put("font_color", randomBrightColor());
                    fancy.put("font_size", 8.5);
                    fancy.put("border_width", 0.8);
                    fancy.put("border_color", "#8A2BE2");
                    fancy.put("shadow_enabled", true);
                    fancy.put("shadow_alpha", 0.9);
                    double dy = -0.74 + ThreadLocalRandom.current().nextDouble(0.0, 0.08);
                    double dx = -0.15 + ThreadLocalRandom.current().nextDouble(0.0, 0.30);
                    fancy.put("transform_y", dy);
                    fancy.put("transform_x", dx);
                    String fancyIntro = getRandomTextIntro();
                    String fancyOutro = getRandomTextOutro();
                    fancy.put("intro_animation", fancyIntro != null ? fancyIntro : textIntro);
                    fancy.put("intro_duration", 0.5);
                    fancy.put("outro_animation", fancyOutro != null ? fancyOutro : textOutro);
                    fancy.put("outro_duration", 0.5);
                    postJson(CAPCUT_API_BASE + PATH_ADD_TEXT, fancy);
                }
            }
        }
    }

    private void processPictures(String draftId, SubtitleFusionV2Request request, String imageIntro, String imageOutro) {
        if (request.getSubtitleInfo() == null || request.getSubtitleInfo().getPictureInfoList() == null) return;
        log.info("[capcut-gen] 处理图片数量: {}", request.getSubtitleInfo().getPictureInfoList().size());
        for (SubtitleFusionV2Request.PictureInfo pi : request.getSubtitleInfo().getPictureInfoList()) {
            if (pi == null || pi.getPictureUrl() == null || pi.getPictureUrl().isEmpty()) continue;
            double start = parseToSeconds(pi.getStartTime());
            double end = parseToSeconds(pi.getEndTime());
            if (end <= start) end = start + 2.0;
            String encodedImageUrl = encodeUrl(pi.getPictureUrl());
            java.util.Map<String, Object> addImage = new java.util.HashMap<>();
            addImage.put("draft_id", draftId);
            addImage.put("image_url", encodedImageUrl);
            addImage.put("start", start);
            addImage.put("end", end);
            addImage.put("track_name", "image_main");
            addImage.put("intro_animation", imageIntro);
            addImage.put("intro_animation_duration", 0.5);
            addImage.put("outro_animation", imageOutro);
            addImage.put("outro_animation_duration", 0.5);
            postJson(CAPCUT_API_BASE + PATH_ADD_IMAGE, addImage);
        }
    }

    private String saveDraft(String draftId) {
        log.info("[capcut-gen] 保存草稿 draftId={}...", draftId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        java.util.Map<String, Object> saveBody = new java.util.HashMap<>();
        saveBody.put("draft_id", draftId);
        ResponseEntity<Map<String, Object>> saveRes = restTemplate.exchange(
                CAPCUT_API_BASE + PATH_SAVE_DRAFT,
                HttpMethod.POST,
                new HttpEntity<>(saveBody, headers),
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        String draftUrl = extractString(saveRes.getBody(), "output", "draft_url");
        log.info("[capcut-gen] 保存完成 draftUrl={}", draftUrl);
        return draftUrl;
    }

    private String getRandomTextIntro() { return randomNameFromListEndpoint(CAPCUT_API_BASE + PATH_GET_TEXT_INTRO_TYPES); }
    private String getRandomTextOutro() { return randomNameFromListEndpoint(CAPCUT_API_BASE + PATH_GET_TEXT_OUTRO_TYPES); }
    private String getRandomImageIntro(String fallback) {
        String v = randomNameFromListEndpoint(CAPCUT_API_BASE + PATH_GET_INTRO_ANIMATION_TYPES);
        return v != null ? v : fallback;
    }
    private String getRandomImageOutro(String fallback) {
        String v = randomNameFromListEndpoint(CAPCUT_API_BASE + PATH_GET_OUTRO_ANIMATION_TYPES);
        return v != null ? v : fallback;
    }

    @Data
    class CapCutGenResponse {
        private boolean success;
        private String draftId;
        private String draftUrl;
        private String message;
    }
    
    private void postJson(String url, java.util.Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
    }

    private String randomNameFromListEndpoint(String url) {
        try {
            ResponseEntity<Map<String, Object>> res = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            Object success = res.getBody() != null ? res.getBody().get("success") : null;
            if (!(success instanceof Boolean) || !((Boolean) success)) {
                return null;
            }
            Object output = res.getBody().get("output");
            if (!(output instanceof List)) return null;
            List<?> list = (List<?>) output;
            if (list.isEmpty()) return null;
            int idx = ThreadLocalRandom.current().nextInt(list.size());
            Object item = list.get(idx);
            if (item instanceof Map) {
                Object name = ((Map<?, ?>) item).get("name");
                return name != null ? String.valueOf(name) : null;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static String extractString(Map<String, Object> body, String key1, String key2) {
        if (body == null) return null;
        Object output = body.get(key1);
        if (!(output instanceof Map)) return null;
        Object val = ((Map<?, ?>) output).get(key2);
        return val != null ? String.valueOf(val) : null;
    }

    private static double parseToSeconds(String s) {
        if (s == null || s.isEmpty()) return 0.0;
        try {
            // 支持 SRT 格式 HH:mm:ss,SSS
            if (s.contains(":")) {
                String[] hhmmss = s.replace('.', ',').split(":");
                if (hhmmss.length == 3) {
                    String secPart = hhmmss[2];
                    int comma = secPart.indexOf(',');
                    int ms = 0;
                    if (comma >= 0) {
                        String msStr = secPart.substring(comma + 1);
                        secPart = secPart.substring(0, comma);
                        if (!msStr.isEmpty()) {
                            // 允许 3 位或更多，统一按毫秒
                            ms = Integer.parseInt(msStr.length() > 3 ? msStr.substring(0, 3) : String.format("%1$-3s", msStr).replace(' ', '0'));
                        }
                    }
                    int hh = Integer.parseInt(hhmmss[0]);
                    int mm = Integer.parseInt(hhmmss[1]);
                    int ss = Integer.parseInt(secPart);
                    return hh * 3600.0 + mm * 60.0 + ss + ms / 1000.0;
                }
            }
            // 默认按秒解析
            return Double.parseDouble(s);
        } catch (Exception ignore) {
            return 0.0;
        }
    }

    private static String randomBrightColor() {
        // 一组高亮/高饱和配色
        String[] palette = new String[]{
                "#FFD400", // 亮黄
                "#FF5C5C", // 亮红
                "#00D1FF", // 亮青
                "#8A2BE2", // 紫
                "#00E676", // 亮绿
                "#FF7F50", // 珊瑚橙
                "#FFA500"  // 橙黄
        };
        int idx = ThreadLocalRandom.current().nextInt(palette.length);
        return palette[idx];
    }
    
    private static String encodeUrl(String raw) {
        if (raw == null || raw.isEmpty()) return raw;
        try {
            URL rawUrl = new URL(raw);
            URI normalizedUri = new URI(
                    rawUrl.getProtocol(),
                    rawUrl.getUserInfo(),
                    rawUrl.getHost(),
                    rawUrl.getPort(),
                    rawUrl.getPath(),
                    rawUrl.getQuery(),
                    null
            );
            return normalizedUri.toASCIIString();
        } catch (Exception e) {
            return raw;
        }
    }

}


