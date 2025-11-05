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

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Map;

@RestController
@RequestMapping("/api/capcut-script-driven")
public class CapCutScriptDrivenController {

   
    


    // CapCutAPI 基础地址（用户已提供的已部署服务）
    private static final String CAPCUT_API_BASE = "http://127.0.0.1:9003";
   

    private final RestTemplate restTemplate = new RestTemplate();

    
    @PostMapping(value = "/capcut-gen", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public CapCutGenResponse submit(@RequestBody SubtitleFusionV2Request request)  {
        CapCutGenResponse resp = new CapCutGenResponse();
        try {
            if (request == null || request.getVideoUrl() == null || request.getVideoUrl().isEmpty()) {
                resp.setSuccess(false);
                resp.setMessage("videoUrl 不能为空");
                return resp;
            }

            // 1) 创建草稿
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // 同时传 height
            java.util.Map<String, Object> draftParams = new java.util.HashMap<>();
            draftParams.put("width", 1080);
            draftParams.put("height", 1920);
            ResponseEntity<Map<String, Object>> draftRes = restTemplate.exchange(
                    CAPCUT_API_BASE + "/create_draft",
                    HttpMethod.POST,
                    new HttpEntity<>(draftParams, headers),
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            String draftId = extractString(draftRes.getBody(), "output", "draft_id");
            if (draftId == null || draftId.isEmpty()) {
                resp.setSuccess(false);
                resp.setMessage("创建草稿失败");
                return resp;
            }
            resp.setDraftId(draftId);

            // 2) 添加视频
            java.util.Map<String, Object> addVideo = new java.util.HashMap<>();
            addVideo.put("draft_id", draftId);
            addVideo.put("video_url", request.getVideoUrl());
            addVideo.put("start", 0);
            addVideo.put("end", 0); // 0 表示整段
            addVideo.put("track_name", "video_main");
            addVideo.put("volume", 1.0);
            postJson(CAPCUT_API_BASE + "/add_video", addVideo);

            // 3) 随机获取字幕动效（文字入/出场）
            String textIntro = randomNameFromListEndpoint(CAPCUT_API_BASE + "/get_text_intro_types");
            String textOutro = randomNameFromListEndpoint(CAPCUT_API_BASE + "/get_text_outro_types");
            if (textIntro == null) textIntro = "Throw_Out"; // 兜底
            if (textOutro == null) textOutro = "Fade_Out";  // 兜底

            // 4) 为每条字幕用 add_text 叠加，并设置随机动效
            if (request.getSubtitleInfo() != null && request.getSubtitleInfo().getCommonSubtitleInfoList() != null) {
                for (SubtitleFusionV2Request.CommonSubtitleInfo si : request.getSubtitleInfo().getCommonSubtitleInfoList()) {
                    if (si == null || si.getText() == null || si.getText().isEmpty()) continue;
                    double start = parseToSeconds(si.getStartTime());
                    double end = parseToSeconds(si.getEndTime());
                    if (end <= start) {
                        end = start + 1.0; // 保底 1 秒
                    }
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
                    addText.put("transform_y", -0.8); // 底部居中
                    addText.put("intro_animation", textIntro);
                    addText.put("intro_duration", 0.5);
                    addText.put("outro_animation", textOutro);
                    addText.put("outro_duration", 0.5);
                    postJson(CAPCUT_API_BASE + "/add_text", addText);

                    // 针对关键词叠加更花的特效（独立 add_text 覆盖层）
                    if (si.getSubtitleEffectInfo() != null && si.getSubtitleEffectInfo().getKeyWords() != null) {
                        for (String kw : si.getSubtitleEffectInfo().getKeyWords()) {
                            if (kw == null || kw.isEmpty()) continue;
                            java.util.Map<String, Object> fancy = new java.util.HashMap<>();
                            fancy.put("draft_id", draftId);
                            fancy.put("text", kw);
                            fancy.put("start", start);
                            fancy.put("end", end);
                            fancy.put("track_name", "text_fx");
                            // 更亮的配色与更大字号
                            fancy.put("font", "文轩体");
                            fancy.put("font_color", randomBrightColor());
                            fancy.put("font_size", 8.5);
                            fancy.put("border_width", 0.8);
                            fancy.put("border_color", "#8A2BE2");
                            fancy.put("shadow_enabled", true);
                            fancy.put("shadow_alpha", 0.9);
                            // 轻微偏移，避免与整句字幕完全重叠
                            double dy = -0.74 + ThreadLocalRandom.current().nextDouble(0.0, 0.08);
                            double dx = -0.15 + ThreadLocalRandom.current().nextDouble(0.0, 0.30);
                            fancy.put("transform_y", dy);
                            fancy.put("transform_x", dx);
                            // 随机再挑一次动效，增强“花”感
                            String fancyIntro = randomNameFromListEndpoint(CAPCUT_API_BASE + "/get_text_intro_types");
                            String fancyOutro = randomNameFromListEndpoint(CAPCUT_API_BASE + "/get_text_outro_types");
                            fancy.put("intro_animation", fancyIntro != null ? fancyIntro : textIntro);
                            fancy.put("intro_duration", 0.5);
                            fancy.put("outro_animation", fancyOutro != null ? fancyOutro : textOutro);
                            fancy.put("outro_duration", 0.5);
                            postJson(CAPCUT_API_BASE + "/add_text", fancy);
                        }
                    }
                }
            }

            // 5) 随机获取图片入/出场动效
            String imageIntro = randomNameFromListEndpoint(CAPCUT_API_BASE + "/get_intro_animation_types");
            String imageOutro = randomNameFromListEndpoint(CAPCUT_API_BASE + "/get_outro_animation_types");
            if (imageIntro == null) imageIntro = textIntro; // 兜底沿用文字动效名
            if (imageOutro == null) imageOutro = textOutro;

            // 6) 添加图片（带随机动效）
            if (request.getSubtitleInfo() != null && request.getSubtitleInfo().getPictureInfoList() != null) {
                for (SubtitleFusionV2Request.PictureInfo pi : request.getSubtitleInfo().getPictureInfoList()) {
                    if (pi == null || pi.getPictureUrl() == null || pi.getPictureUrl().isEmpty()) continue;
                    double start = parseToSeconds(pi.getStartTime());
                    double end = parseToSeconds(pi.getEndTime());
                    if (end <= start) end = start + 2.0;
                    java.util.Map<String, Object> addImage = new java.util.HashMap<>();
                    addImage.put("draft_id", draftId);
                    addImage.put("image_url", pi.getPictureUrl());
                    addImage.put("start", start);
                    addImage.put("end", end);
                    addImage.put("track_name", "image_main");
                    addImage.put("intro_animation", imageIntro);
                    addImage.put("intro_animation_duration", 0.5);
                    addImage.put("outro_animation", imageOutro);
                    addImage.put("outro_animation_duration", 0.5);
                    postJson(CAPCUT_API_BASE + "/add_image", addImage);
                }
            }

            // 7) 保存草稿
            java.util.Map<String, Object> saveBody = new java.util.HashMap<>();
            saveBody.put("draft_id", draftId);
            ResponseEntity<Map<String, Object>> saveRes = restTemplate.exchange(
                    CAPCUT_API_BASE + "/save_draft",
                    HttpMethod.POST,
                    new HttpEntity<>(saveBody, headers),
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            String draftUrl = extractString(saveRes.getBody(), "output", "draft_url");
            resp.setSuccess(true);
            resp.setDraftUrl(draftUrl);
            resp.setMessage("OK");
            return resp;
        } catch (Exception e) {
            resp.setSuccess(false);
            resp.setMessage(e.getMessage());
            return resp;
        }
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
        
}


