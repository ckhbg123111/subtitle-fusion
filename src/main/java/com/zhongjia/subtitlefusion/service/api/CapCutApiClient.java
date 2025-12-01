package com.zhongjia.subtitlefusion.service.api;

import com.zhongjia.subtitlefusion.model.CapCutCloudResponse;
import com.zhongjia.subtitlefusion.model.CapCutCloudTaskStatus;
import com.zhongjia.subtitlefusion.model.capcut.GenerateVideoOutput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhongjia.subtitlefusion.model.capcut.CapCutResponse;
import com.zhongjia.subtitlefusion.model.capcut.DraftRefOutput;

import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
@RequiredArgsConstructor
public class CapCutApiClient {

    private static final String PATH_CREATE_DRAFT = "/create_draft";
    private static final String PATH_ADD_VIDEO = "/add_video";
    private static final String PATH_ADD_AUDIO = "/add_audio";
    private static final String PATH_ADD_TEXT = "/add_text";
    private static final String PATH_ADD_IMAGE = "/add_image";
    private static final String PATH_SAVE_DRAFT = "/save_draft";
    private static final String PATH_GET_TEXT_INTRO_TYPES = "/get_text_intro_types";
    private static final String PATH_GET_TEXT_OUTRO_TYPES = "/get_text_outro_types";
    private static final String PATH_GET_INTRO_ANIMATION_TYPES = "/get_intro_animation_types";
    private static final String PATH_GET_OUTRO_ANIMATION_TYPES = "/get_outro_animation_types";
    private static final String PATH_ADD_TEXT_TEMPLATE = "/add_text_template";
    private static final String PATH_SEARCH_STICKER = "/search_sticker";
    private static final String PATH_ADD_STICKER = "/add_sticker";
    private static final String PATH_GET_TEXT_LOOP_ANIM_TYPES = "/get_text_loop_anim_types";
    private static final String PATH_GET_FONT_TYPES = "/get_font_types";
    private static final String PATH_GENERATE_VIDEO = "/generate_video";
    private static final String PATH_TASK_STATUS = "/task_status";
    private static final String PATH_GET_DURATION = "/get_duration";

    private final RestTemplate restTemplate = new RestTemplate();
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${capcut.api.base:https://open.capcutapi.top/cut_jianying}")
    private String capcutApiBase;

    @Value("${capcut.api.key:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJvY2FQTjZfTklja3diQ3hrQlRSZlNYaEM3VGhZIiwiaWF0IjowfQ.JmYiGfddux0FEryWvJp0G1rC_WV08f269jHOd-lW1ArWl1SreuAk7SCU15Kx3HfmdO1BB9nQJ2ooNPqiTyU1SUYEYjgQbd_2QpNsmuWzxoUJg2wx6RqKtAl3ymV5KTIbLMw1hjNCoPIZd2hwu9yhUSQeHQ7WlkyzhG1pllZQeQvnjefX4MgG7LlNn7jF_V7ExhSdFvJCAFiq_BBQnjK9B1SGnxtLqtyiusfZRo5rZz-5WeJN9kzYdmSbtaBtc8-aHSzkc17dvTe8XAeKLv5yALn3rf7uhWyeVb2377SZHkIRra6dLLOdwxScvHKz_ewCliBT_XF-M0K_ioglqc4OhA}")
    private String capcutApiKey;

    @Value("${capcut.draft.folder:C:\\Users\\Administrator01\\AppData\\Local\\JianyingPro\\User Data\\Projects\\com.lveditor.draft}")
    private String draftFolder;

    @Value("${capcut.is_capcut:0}")
    private int isCapcut;

    @Value("${capcut.license.key:}")
    private String licenseKey;

    /**
     * 强类型返回 /create_draft 响应体
     */
    public com.zhongjia.subtitlefusion.model.capcut.CapCutResponse<com.zhongjia.subtitlefusion.model.capcut.DraftRefOutput> createDraft(Integer width, Integer height) {
        java.util.Map<String, Object> draftParams = new java.util.HashMap<>();
        if (width != null) draftParams.put("width", width);
        if (height != null) draftParams.put("height", height);
        return postJsonFor(capcutApiBase + PATH_CREATE_DRAFT, draftParams, DraftRefOutput.class);
    }

    public CapCutResponse<DraftRefOutput> addVideo(String draftId, String videoUrl, double start, double end, String trackName, double volume) {
        String encodedUrl = encodeUrl(videoUrl);
        java.util.Map<String, Object> addVideo = new java.util.HashMap<>();
        addVideo.put("draft_id", draftId);
        addVideo.put("video_url", encodedUrl);
        addVideo.put("start", start);
        addVideo.put("end", end);
        addVideo.put("track_name", trackName);
        addVideo.put("volume", volume);
        return postJsonFor(capcutApiBase + PATH_ADD_VIDEO, addVideo, DraftRefOutput.class);
    }

    public CapCutResponse<DraftRefOutput> addText(Map<String, Object> params) {
        return postJsonFor(capcutApiBase + PATH_ADD_TEXT, params, DraftRefOutput.class);
    }

    public CapCutResponse<DraftRefOutput> addImage(Map<String, Object> params) {
        return postJsonFor(capcutApiBase + PATH_ADD_IMAGE, params, DraftRefOutput.class);
    }

    /**
     * 以 Map 直传参数，支持 target_start/transition/transition_duration/track_name 等完整字段
     */
    public CapCutResponse<DraftRefOutput> addVideo(Map<String, Object> params) {
        return postJsonFor(capcutApiBase + PATH_ADD_VIDEO, params, DraftRefOutput.class);
    }

    /**
     * 添加音频，支持 volume/fade_in/out/target_start 等字段
     */
    public CapCutResponse<DraftRefOutput> addAudio(Map<String, Object> params) {
        return postJsonFor(capcutApiBase + PATH_ADD_AUDIO, params, DraftRefOutput.class);
    }

    public CapCutResponse<DraftRefOutput> addTextTemplate(Map<String, Object> params) {
        return postJsonFor(capcutApiBase + PATH_ADD_TEXT_TEMPLATE, params, DraftRefOutput.class);
    }

    public CapCutResponse<DraftRefOutput> addSticker(Map<String, Object> params) {
        return postJsonFor(capcutApiBase + PATH_ADD_STICKER, params, DraftRefOutput.class);
    }

    public List<String> getTextLoopAnimationTypes() {
        return getNamesFromCacheOrRemote("capcut:types:text-loop", capcutApiBase + PATH_GET_TEXT_LOOP_ANIM_TYPES);
    }

    public List<String> getFontTypes() {
        return getNamesFromCacheOrRemote("capcut:types:fonts", capcutApiBase + PATH_GET_FONT_TYPES);
    }

    public String saveDraft(String draftId) {
        HttpHeaders headers = buildJsonHeaders();
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("draft_id", draftId);
        body.put("draft_folder", draftFolder);
        body.put("is_capcut", isCapcut);
        ResponseEntity<Map<String, Object>> saveRes = restTemplate.exchange(
                capcutApiBase + PATH_SAVE_DRAFT,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        String draftUrl = extractString(saveRes.getBody(), "output", "draft_url");
        log.info("[CapCutApi] saveDraft -> {}", draftUrl);
        return draftUrl;
    }

    public String getRandomTextIntro() {
        List<String> list = getNamesFromCacheOrRemote("capcut:types:text-intro", capcutApiBase + PATH_GET_TEXT_INTRO_TYPES);
        return chooseRandom(list);
    }
    public String getRandomTextOutro() {
        List<String> list = getNamesFromCacheOrRemote("capcut:types:text-outro", capcutApiBase + PATH_GET_TEXT_OUTRO_TYPES);
        return chooseRandom(list);
    }
    public String getRandomImageIntro(String fallback) {
        List<String> list = getNamesFromCacheOrRemote("capcut:types:image-intro", capcutApiBase + PATH_GET_INTRO_ANIMATION_TYPES);
        String v = chooseRandom(list);
        return v != null ? v : fallback;
    }
    public String getRandomImageOutro(String fallback) {
        List<String> list = getNamesFromCacheOrRemote("capcut:types:image-outro", capcutApiBase + PATH_GET_OUTRO_ANIMATION_TYPES);
        String v = chooseRandom(list);
        return v != null ? v : fallback;
    }

    public String encodeUrl(String raw) {
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

    private <T> CapCutResponse<T> postJsonFor(String url, java.util.Map<String, Object> body, Class<T> outputClass) {
        HttpHeaders headers = buildJsonHeaders();
        ResponseEntity<Map<String, Object>> res = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        CapCutResponse<T> result = new CapCutResponse<>();
        try {
            Map<String, Object> resp = res.getBody();
            if (resp == null) {
                result.setSuccess(false);
                result.setError("empty response");
                return result;
            }
            Object success = resp.get("success");
            result.setSuccess(success instanceof Boolean && (Boolean) success);
            Object error = resp.get("error");
            result.setError(error != null ? String.valueOf(error) : null);
            Object purchaseLink = resp.get("purchase_link");
            result.setPurchaseLink(purchaseLink != null ? String.valueOf(purchaseLink) : null);
            Object output = resp.get("output");
            // 兼容 output 为空字符串的情况：视为无输出（null）
            if (output instanceof String) {
                String s = ((String) output).trim();
                if (s.isEmpty()) {
                    output = null;
                }
            }
            if (output != null) {
                @SuppressWarnings("unchecked")
                T parsed = (T) objectMapper.convertValue(output, outputClass);
                result.setOutput(parsed);
            }
            if (!result.isSuccess()) {
                log.warn("[CapCutApi] request failed url={}, body={}, resp={}", url, body, resp);
            }
            return result;
        } catch (Exception e) {
            log.warn("[CapCutApi] parse response failed url={}, body={}, err={}", url, body, e.getMessage());
            result.setSuccess(false);
            result.setError(e.getMessage());
            return result;
        }
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
                log.warn("[CapCutApi] request failed url={}, body={}, resp={}", url, body, resp);
            }
        } catch (Exception ignore) {
        }
    }

    /**
     * 提交云渲染任务，返回强类型响应（包含 task_id 与 success/error）
     */
    public CapCutResponse<GenerateVideoOutput> generateVideo(String draftId, String resolution, String framerate) {
        if (draftId == null || draftId.isEmpty()) {
            throw new IllegalArgumentException("draftId 不能为空");
        }
        if (licenseKey == null || licenseKey.isEmpty()) {
            throw new IllegalStateException("capcut.license.key 未配置");
        }
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("draft_id", draftId);
        body.put("license_key", licenseKey);
        if (resolution != null && !resolution.isEmpty()) body.put("resolution", resolution);
        if (framerate != null && !framerate.isEmpty()) body.put("framerate", framerate);
        com.zhongjia.subtitlefusion.model.capcut.CapCutResponse<com.zhongjia.subtitlefusion.model.capcut.GenerateVideoOutput> result =
                postJsonFor(capcutApiBase + PATH_GENERATE_VIDEO, body, com.zhongjia.subtitlefusion.model.capcut.GenerateVideoOutput.class);
        if (result != null && result.getOutput() != null) {
            log.info("[CapCutApi] generateVideo -> {}", result.getOutput().getTaskId());
        } else {
            log.warn("[CapCutApi] generateVideo -> empty output");
        }
        return result;
    }

    /**
     * 查询云渲染任务状态
     */
    public CapCutCloudResponse<CapCutCloudTaskStatus> taskStatus(String taskId) {
        if (taskId == null || taskId.isEmpty()) throw new IllegalArgumentException("taskId 不能为空");
        HttpHeaders headers = buildJsonHeaders();
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("task_id", taskId);
        ResponseEntity<Map<String, Object>> res = restTemplate.exchange(
                capcutApiBase + PATH_TASK_STATUS,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        CapCutCloudResponse<CapCutCloudTaskStatus> result = new CapCutCloudResponse<>();
        try {
            Map<String, Object> resp = res.getBody();
            if (resp == null) {
                result.setSuccess(false);
                result.setError("empty response");
                return result;
            }
            Object success = resp.get("success");
            result.setSuccess(success instanceof Boolean && (Boolean) success);
            Object error = resp.get("error");
            result.setError(error != null ? String.valueOf(error) : null);
            Object output = resp.get("output");
            // 兼容 output 为空字符串
            if (output instanceof String) {
                String s = ((String) output).trim();
                if (s.isEmpty()) {
                    output = null;
                }
            }
            if (output != null) {
                CapCutCloudTaskStatus status = objectMapper.convertValue(output, CapCutCloudTaskStatus.class);
                result.setOutput(status);
            }
            if (!Boolean.TRUE.equals(result.getSuccess())) {
                log.warn("[CapCutApi] taskStatus failed taskId={}, resp={}", taskId, resp);
            }
            return result;
        } catch (Exception e) {
            log.warn("[CapCutApi] taskStatus parse failed taskId={}, err={}", taskId, e.getMessage());
            result.setSuccess(false);
            result.setError(e.getMessage());
            return result;
        }
    }

    /**
     * 远程探测音/视频时长（秒）。失败返回 null。
     */
    public Double getDuration(String url) {
        if (url == null || url.isEmpty()) return null;
        try {
            HttpHeaders headers = buildJsonHeaders();
            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("url", encodeUrl(url));
            ResponseEntity<Map<String, Object>> res = restTemplate.exchange(
                    capcutApiBase + PATH_GET_DURATION,
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            Map<String, Object> resp = res.getBody();
            if (resp == null) return null;
            Object success = resp.get("success");
            if (!(success instanceof Boolean) || !((Boolean) success)) return null;
            Object output = resp.get("output");
            if (!(output instanceof Map)) return null;
            Object duration = ((Map<?, ?>) output).get("duration");
            if (duration == null) return null;
            return Double.valueOf(String.valueOf(duration));
        } catch (Exception e) {
            log.warn("[CapCutApi] getDuration failed url={}, err={}", url, e.getMessage());
            return null;
        }
    }

    private List<String> getNamesFromCacheOrRemote(String cacheKey, String url) {
        // 优先读取缓存
        try {
            if (redisTemplate != null) {
                String cached = redisTemplate.opsForValue().get(cacheKey);
                if (cached != null && !cached.isEmpty()) {
                    List<String> names = objectMapper.readValue(cached, new TypeReference<List<String>>() {});
                    if (names != null && !names.isEmpty()) {
                        return names;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[CapCutApi] read cache failed key={}, err={}", cacheKey, e.getMessage());
        }

        // 缓存未命中，走远程
        List<String> remote = fetchAllNames(url);
        if (remote != null && !remote.isEmpty()) {
            try {
                if (redisTemplate != null) {
                    String json = objectMapper.writeValueAsString(remote);
                    redisTemplate.opsForValue().set(cacheKey, json, Duration.ofHours(6));
                }
            } catch (Exception e) {
                log.warn("[CapCutApi] write cache failed key={}, err={}", cacheKey, e.getMessage());
            }
        }
        return remote;
    }

    private List<String> fetchAllNames(String url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + capcutApiKey);
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
            if (list.isEmpty()) return new ArrayList<>();
            List<String> names = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Map) {
                    Object name = ((Map<?, ?>) item).get("name");
                    if (name != null) names.add(String.valueOf(name));
                }
            }
            return names;
        } catch (Exception e) {
            return null;
        }
    }

    private String chooseRandom(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        int idx = ThreadLocalRandom.current().nextInt(list.size());
        return list.get(idx);
    }

    private HttpHeaders buildJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + capcutApiKey);
        return headers;
    }

    private static String extractString(Map<String, Object> body, String key1, String key2) {
        if (body == null) return null;
        Object output = body.get(key1);
        if (!(output instanceof Map)) return null;
        Object val = ((Map<?, ?>) output).get(key2);
        return val != null ? String.valueOf(val) : null;
    }
}


