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
    private static final String CAPCUT_API_BASE = "https://open.capcutapi.top/cut_jianying";
    // 接口鉴权：硬编码 API Key（如需安全可改为环境变量注入）
    private static final String CAPCUT_API_KEY = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJvY2FQTjZfTklja3diQ3hrQlRSZlNYaEM3VGhZIiwiaWF0IjowfQ.JmYiGfddux0FEryWvJp0G1rC_WV08f269jHOd-lW1ArWl1SreuAk7SCU15Kx3HfmdO1BB9nQJ2ooNPqiTyU1SUYEYjgQbd_2QpNsmuWzxoUJg2wx6RqKtAl3ymV5KTIbLMw1hjNCoPIZd2hwu9yhUSQeHQ7WlkyzhG1pllZQeQvnjefX4MgG7LlNn7jF_V7ExhSdFvJCAFiq_BBQnjK9B1SGnxtLqtyiusfZRo5rZz-5WeJN9kzYdmSbtaBtc8-aHSzkc17dvTe8XAeKLv5yALn3rf7uhWyeVb2377SZHkIRra6dLLOdwxScvHKz_ewCliBT_XF-M0K_ioglqc4OhA";
    // save_draft 所需参数：草稿路径与平台标志（剪映=0，CapCut=1）
    private static final String CAPCUT_DRAFT_FOLDER = "C:\\Users\\Administrator01\\AppData\\Local\\JianyingPro\\User Data\\Projects\\com.lveditor.draft";
    private static final int CAPCUT_IS_CAPCUT = 0;
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
        HttpHeaders headers = buildJsonHeaders();
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
            boolean hasKeywords = si.getSubtitleEffectInfo() != null
                    && si.getSubtitleEffectInfo().getKeyWords() != null
                    && !si.getSubtitleEffectInfo().getKeyWords().isEmpty();
            java.util.Map<String, Object> addText = new java.util.HashMap<>();
            addText.put("draft_id", draftId);
            addText.put("text", si.getText());
            addText.put("start", start);
            addText.put("end", end);
            addText.put("track_name", "text_fx");
            // 使用 CapCut API 支持的中文字体
            addText.put("font", "SourceHanSansCN_Regular");
            addText.put("font_color", "#FFFFFF");
            addText.put("font_size", 6);
            addText.put("border_width", 1);
            addText.put("border_color", "#000000");
            addText.put("shadow_enabled", true);
            addText.put("shadow_alpha", 0.8);
            addText.put("transform_y", -0.8);
            // 当无关键词时整句可以有随机动效；有关键词时整句普通展示（不加动效）
            if (!hasKeywords) {
                addText.put("intro_animation", textIntro != null ? textIntro : "Throw_Out");
                addText.put("intro_duration", 0.5);
                addText.put("outro_animation", textOutro != null ? textOutro : "Fade_Out");
                addText.put("outro_duration", 0.5);
            }
            postJson(CAPCUT_API_BASE + PATH_ADD_TEXT, addText);

            if (hasKeywords) {
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
                    fancy.put("font_size", 9);
                    fancy.put("border_width", 1);
                    fancy.put("border_color", "#8A2BE2");
                    fancy.put("shadow_enabled", true);
                    fancy.put("shadow_alpha", 0.9);
                    double dy = -0.74 + ThreadLocalRandom.current().nextDouble(0.0, 0.08);
                    double dx = -0.15 + ThreadLocalRandom.current().nextDouble(0.0, 0.30);
                    fancy.put("transform_y", dy);
                    fancy.put("transform_x", dx);
                    // 关键词仅用特殊字体着重标出，不添加动效
                    postJson(CAPCUT_API_BASE + PATH_ADD_TEXT, fancy);
                }
            }
        }
    }

    private void processPictures(String draftId, SubtitleFusionV2Request request, String imageIntro, String imageOutro) {
        if (request.getSubtitleInfo() == null || request.getSubtitleInfo().getPictureInfoList() == null) return;
        List<SubtitleFusionV2Request.PictureInfo> pics = new java.util.ArrayList<>(request.getSubtitleInfo().getPictureInfoList());
        // 先按开始时间排序，便于最少轨道分配
        pics.sort(new java.util.Comparator<SubtitleFusionV2Request.PictureInfo>() {
            @Override
            public int compare(SubtitleFusionV2Request.PictureInfo a, SubtitleFusionV2Request.PictureInfo b) {
                double sa = parseToSeconds(a != null ? a.getStartTime() : null);
                double sb = parseToSeconds(b != null ? b.getStartTime() : null);
                return Double.compare(sa, sb);
            }
        });
        log.info("[capcut-gen] 处理图片数量: {}", pics.size());

        // 轨道结束时间表，避免同一轨道时间段重叠
        java.util.List<Double> laneEnds = new java.util.ArrayList<>();
        final double EPS = 1e-3; // 允许毫秒级收口，避免浮点数微重叠

        for (SubtitleFusionV2Request.PictureInfo pi : pics) {
            if (pi == null || pi.getPictureUrl() == null || pi.getPictureUrl().isEmpty()) continue;
            double start = parseToSeconds(pi.getStartTime());
            double end = parseToSeconds(pi.getEndTime());
            if (end <= start) end = start + 2.0;

            // 分配到第一个可用轨道（开始时间不小于该轨道的结束时间）
            int lane = -1;
            for (int i = 0; i < laneEnds.size(); i++) {
                if (start >= laneEnds.get(i) - EPS) { lane = i; break; }
            }
            if (lane < 0) { lane = laneEnds.size(); laneEnds.add(0.0); }
            laneEnds.set(lane, end);

            String trackName = lane == 0 ? "image_main" : ("image_main_" + lane);
            String encodedImageUrl = encodeUrl(pi.getPictureUrl());
            java.util.Map<String, Object> addImage = new java.util.HashMap<>();
            addImage.put("draft_id", draftId);
            addImage.put("image_url", encodedImageUrl);
            addImage.put("start", start);
            addImage.put("end", end);
            addImage.put("track_name", trackName);
            // 无位移时传 0（数值类型）
            addImage.put("transform_y_px", 0);
            addImage.put("transform_x_px", 0);
            addImage.put("intro_animation", imageIntro);
            addImage.put("intro_animation_duration", 0.5);
            addImage.put("outro_animation", imageOutro);
            addImage.put("outro_animation_duration", 0.5);
            postJson(CAPCUT_API_BASE + PATH_ADD_IMAGE, addImage);
        }
    }

    private String saveDraft(String draftId) {
        log.info("[capcut-gen] 保存草稿 draftId={}...", draftId);
        HttpHeaders headers = buildJsonHeaders();
        java.util.Map<String, Object> saveBody = new java.util.HashMap<>();
        saveBody.put("draft_id", draftId);
        // 按照接口文档补齐必须参数
        saveBody.put("draft_folder", CAPCUT_DRAFT_FOLDER);
        saveBody.put("is_capcut", CAPCUT_IS_CAPCUT);
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
        HttpHeaders headers = buildJsonHeaders();
        ResponseEntity<Map<String, Object>> res = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        try {
            Map<String, Object> resp = res.getBody();
            Object success = resp != null ? resp.get("success") : null;
            if (!(success instanceof Boolean) || !((Boolean) success)) {
                log.warn("[capcut-gen] 请求失败 url={}, body={}, resp={}", url, body, resp);
            }
        } catch (Exception e) {
            // 避免日志异常影响主流程
        }
    }

    private String randomNameFromListEndpoint(String url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + CAPCUT_API_KEY);
            ResponseEntity<Map<String, Object>> res = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
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

    private HttpHeaders buildJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + CAPCUT_API_KEY);
        return headers;
    }

    private static String extractString(Map<String, Object> body, String key1, String key2) {
        if (body == null) return null;
        Object output = body.get(key1);
        if (!(output instanceof Map)) return null;
        Object val = ((Map<?, ?>) output).get(key2);
        return val != null ? String.valueOf(val) : null;
    }

    private static double parseToSeconds(String s) {
        if (s == null) return 0.0;
        s = s.trim();
        if (s.isEmpty()) return 0.0;
        try {
            // 通用支持：
            // 1) hh:mm:ss[.SSS]
            // 2) mm:ss[.SSS]
            // 3) ss[.SSS]
            // 同时兼容逗号/点作为毫秒分隔符
            if (s.contains(":")) {
                String[] parts = s.split(":");
                if (parts.length == 3) {
                    int hh = parseIntSafe(parts[0]);
                    int mm = parseIntSafe(parts[1]);
                    double ss = parseSecondWithFraction(parts[2]);
                    return hh * 3600.0 + mm * 60.0 + ss;
                } else if (parts.length == 2) {
                    int mm = parseIntSafe(parts[0]);
                    double ss = parseSecondWithFraction(parts[1]);
                    return mm * 60.0 + ss;
                }
                // 其他带冒号但不符合预期的情况，继续走兜底解析
            }
            // 无冒号或不符合上面格式，尝试直接按秒解析（支持 "12.345"）
            return Double.parseDouble(s.replace(',', '.'));
        } catch (Exception ignore) {
            return 0.0;
        }
    }

    private static int parseIntSafe(String v) {
        if (v == null || v.isEmpty()) return 0;
        // 去掉可能的前导空白
        v = v.trim();
        // 遇到小数/毫秒分隔，只取整数部分
        int dot = v.indexOf('.');
        int comma = v.indexOf(',');
        int cut = -1;
        if (dot >= 0 && comma >= 0) cut = Math.min(dot, comma);
        else if (dot >= 0) cut = dot;
        else if (comma >= 0) cut = comma;
        if (cut >= 0) v = v.substring(0, cut);
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static double parseSecondWithFraction(String secondPart) {
        if (secondPart == null || secondPart.isEmpty()) return 0.0;
        secondPart = secondPart.trim();
        // 统一小数分隔符为 '.'
        String normalized = secondPart.replace(',', '.');
        try {
            // 直接按小数解析，e.g. "01.250" -> 1.25
            return Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            // 如果仍然失败，退回到仅整数秒
            return parseIntSafe(secondPart);
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


