package com.zhongjia.subtitlefusion.service.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class CapCutApiClient {

    private static final String PATH_CREATE_DRAFT = "/create_draft";
    private static final String PATH_ADD_VIDEO = "/add_video";
    private static final String PATH_ADD_TEXT = "/add_text";
    private static final String PATH_ADD_IMAGE = "/add_image";
    private static final String PATH_SAVE_DRAFT = "/save_draft";
    private static final String PATH_GET_TEXT_INTRO_TYPES = "/get_text_intro_types";
    private static final String PATH_GET_TEXT_OUTRO_TYPES = "/get_text_outro_types";
    private static final String PATH_GET_INTRO_ANIMATION_TYPES = "/get_intro_animation_types";
    private static final String PATH_GET_OUTRO_ANIMATION_TYPES = "/get_outro_animation_types";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${capcut.api.base:https://open.capcutapi.top/cut_jianying}")
    private String capcutApiBase;

    @Value("${capcut.api.key:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJvY2FQTjZfTklja3diQ3hrQlRSZlNYaEM3VGhZIiwiaWF0IjowfQ.JmYiGfddux0FEryWvJp0G1rC_WV08f269jHOd-lW1ArWl1SreuAk7SCU15Kx3HfmdO1BB9nQJ2ooNPqiTyU1SUYEYjgQbd_2QpNsmuWzxoUJg2wx6RqKtAl3ymV5KTIbLMw1hjNCoPIZd2hwu9yhUSQeHQ7WlkyzhG1pllZQeQvnjefX4MgG7LlNn7jF_V7ExhSdFvJCAFiq_BBQnjK9B1SGnxtLqtyiusfZRo5rZz-5WeJN9kzYdmSbtaBtc8-aHSzkc17dvTe8XAeKLv5yALn3rf7uhWyeVb2377SZHkIRra6dLLOdwxScvHKz_ewCliBT_XF-M0K_ioglqc4OhA}")
    private String capcutApiKey;

    @Value("${capcut.draft.folder:C:\\Users\\Administrator01\\AppData\\Local\\JianyingPro\\User Data\\Projects\\com.lveditor.draft}")
    private String draftFolder;

    @Value("${capcut.is_capcut:0}")
    private int isCapcut;

    public String createDraft(int width, int height) {
        HttpHeaders headers = buildJsonHeaders();
        java.util.Map<String, Object> draftParams = new java.util.HashMap<>();
        draftParams.put("width", width);
        draftParams.put("height", height);
        ResponseEntity<Map<String, Object>> draftRes = restTemplate.exchange(
                capcutApiBase + PATH_CREATE_DRAFT,
                HttpMethod.POST,
                new HttpEntity<>(draftParams, headers),
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        String draftId = extractString(draftRes.getBody(), "output", "draft_id");
        log.info("[CapCutApi] createDraft -> {}", draftId);
        return draftId;
    }

    public void addVideo(String draftId, String videoUrl, double start, double end, String trackName, double volume) {
        String encodedUrl = encodeUrl(videoUrl);
        java.util.Map<String, Object> addVideo = new java.util.HashMap<>();
        addVideo.put("draft_id", draftId);
        addVideo.put("video_url", encodedUrl);
        addVideo.put("start", start);
        addVideo.put("end", end);
        addVideo.put("track_name", trackName);
        addVideo.put("volume", volume);
        postJson(capcutApiBase + PATH_ADD_VIDEO, addVideo);
    }

    public void addText(Map<String, Object> params) {
        postJson(capcutApiBase + PATH_ADD_TEXT, params);
    }

    public void addImage(Map<String, Object> params) {
        postJson(capcutApiBase + PATH_ADD_IMAGE, params);
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

    public String getRandomTextIntro() { return randomNameFromListEndpoint(capcutApiBase + PATH_GET_TEXT_INTRO_TYPES); }
    public String getRandomTextOutro() { return randomNameFromListEndpoint(capcutApiBase + PATH_GET_TEXT_OUTRO_TYPES); }
    public String getRandomImageIntro(String fallback) {
        String v = randomNameFromListEndpoint(capcutApiBase + PATH_GET_INTRO_ANIMATION_TYPES);
        return v != null ? v : fallback;
    }
    public String getRandomImageOutro(String fallback) {
        String v = randomNameFromListEndpoint(capcutApiBase + PATH_GET_OUTRO_ANIMATION_TYPES);
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

    private String randomNameFromListEndpoint(String url) {
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


