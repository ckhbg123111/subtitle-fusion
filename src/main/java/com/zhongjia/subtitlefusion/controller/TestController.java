package com.zhongjia.subtitlefusion.controller;

import com.zhongjia.subtitlefusion.model.Result;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试接口：演示「N 张图片拼接生成草稿（时间轴为图片序列）」能力。
 *
 * - POST /api/test/draft-from-images ：传入图片 URL 列表，后端调用 CapCut API 创建草稿并按顺序铺在时间线上，返回 draftId 与 draftUrl。
 * - GET  /api/test/draft-url        ：仅返回一个固定示例 URL，方便快速联通测试。
 */
@RestController
@RequestMapping("/api/test")
@Slf4j
public class TestController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${capcut.api.base:https://cut-jianying-vdvswivepm.cn-hangzhou.fcapp.run}")
    private String capcutApiBase;

    @Value("${capcut.api.key:eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJvY2FQTjZfTklja3diQ3hrQlRSZlNYaEM3VGhZIiwiaWF0IjowfQ.JmYiGfddux0FEryWvJp0G1rC_WV08f269jHOd-lW1ArWl1SreuAk7SCU15Kx3HfmdO1BB9nQJ2ooNPqiTyU1SUYEYjgQbd_2QpNsmuWzxoUJg2wx6RqKtAl3ymV5KTIbLMw1hjNCoPIZd2hwu9yhUSQeHQ7WlkyzhG1pllZQeQvnjefX4MgG7LlNn7jF_V7ExhSdFvJCAFiq_BBQnjK9B1SGnxtLqtyiusfZRo5rZz-5WeJN9kzYdmSbtaBtc8-aHSzkc17dvTe8XAeKLv5yALn3rf7uhWyeVb2377SZHkIRra6dLLOdwxScvHKz_ewCliBT_XF-M0K_ioglqc4OhA}")
    private String capcutApiKey;

    /**
     * 简单联通测试：返回一个示例草稿 URL（固定值）
     *
     * GET /api/test/draft-url
     */
    @GetMapping(value = "/draft-url", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getDraftUrl() {
        Map<String, Object> resp = new HashMap<>();
        String demoDraftUrl = "https://www.install-ai-guider.top/draft/downloader?draft_id=demo_draft_id";
        resp.put("draftUrl", demoDraftUrl);
        return resp;
    }

    /**
     * 核心测试接口：N 张图片生成一个草稿（时间轴为图片轮播），返回 draftId 与 draftUrl
     *
     * - 图片顺序：按 imageUrls 的顺序依次排布；
     * - 时长分配：如果未指定 totalDurationSeconds，则默认整体 20s，均分到每张图片；
     * - 画布尺寸：未指定 width/height 时，默认 1080x1920。
     *
     * 示例请求：
     * POST /api/test/draft-from-images
     * Content-Type: application/json
     * {
     *   "imageUrls": ["https://xxx/1.png", "https://xxx/2.png", "https://xxx/3.png"],
     *   "totalDurationSeconds": 20,
     *   "width": 1080,
     *   "height": 1920
     * }
     */
    @PostMapping(value = "/draft-from-images", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Result<Map<String, Object>> createDraftFromImages(@RequestBody ImagesToDraftRequest request) {
        if (request == null || request.getImageUrls() == null || request.getImageUrls().isEmpty()) {
            return Result.error("imageUrls 不能为空");
        }

        List<String> imageUrls = request.getImageUrls();
        int n = imageUrls.size();

        double totalDuration = request.getTotalDurationSeconds() != null && request.getTotalDurationSeconds() > 0
                ? request.getTotalDurationSeconds()
                : 20.0;
        double perDuration = totalDuration / n;

        int width = (request.getWidth() != null && request.getWidth() > 0) ? request.getWidth() : 1080;
        int height = (request.getHeight() != null && request.getHeight() > 0) ? request.getHeight() : 1920;

        // 1. 创建草稿（直接通过 RestTemplate 调用 CapCut /create_draft，避免耦合业务 Service）
        DraftInfo draftInfo = createDraftInternal(width, height);
        if (draftInfo == null || draftInfo.getDraftId() == null || draftInfo.getDraftId().isEmpty()) {
            return Result.error("创建草稿失败");
        }
        String draftId = draftInfo.getDraftId();
        String draftUrl = draftInfo.getDraftUrl();
        log.info("[TestController] 草稿创建成功 draftId={}, draftUrl={}", draftId, draftUrl);

        // 2. 将图片顺序铺在时间轴上，并为每张图片添加关键帧动画
        int effectiveCount = 0;
        for (int i = 0; i < n; i++) {
            String url = imageUrls.get(i);
            if (url == null || url.isEmpty()) {
                continue;
            }
            double start = perDuration * i;
            double end = (i == n - 1) ? totalDuration : perDuration * (i + 1);
            String trackName = "image_main";

            // 2.1 添加撑满画布的图片片段
            addImageInternal(draftId, url, start, end, trackName);

            // 2.2 为当前图片时间段添加关键帧，形成「漫剧」效果
            addComicKeyframesSingleTrack(draftId, start, end, i, trackName, width, height);
            effectiveCount++;
        }

        if (effectiveCount == 0) {
            return Result.error("有效的图片 URL 为空，无法生成草稿");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("draftId", draftId);
        data.put("draftUrl", draftUrl);
        data.put("totalDurationSeconds", totalDuration);
        data.put("imageCount", effectiveCount);
        return Result.success(data);
    }

    /**
     * 为单张图片所在的时间段添加关键帧：缩放 + 轻微平移/旋转 + 透明度渐变
     * 关键帧覆盖整个 [start, end] 区间，实现「漫剧」式缓慢运动。
     */
    /**
     * 单轨道版本：所有图片都在同一轨道上（image_main）。
     * 为避免“轨道级关键帧”在不同图片间互相影响：
     * - 关键帧全部落在该图片的 (start, end) 区间内（不与相邻图片重叠/同时间点）；
     * - 在区间尾部做淡出，并将位移/旋转/缩放回中复位，让下一张的入场不被上一张遗留状态影响。
     */
    private void addComicKeyframesSingleTrack(String draftId, double start, double end, int index, String trackName, int width, int height) {
        double duration = Math.max(0.1, end - start);

        // 关键帧边界内缩：避免与相邻片段的边界时间点重叠
        double eps = Math.min(0.08, duration * 0.12);
        eps = Math.min(eps, duration * 0.45);
        double segStart = start + eps;
        double segEnd = end - eps;
        if (segEnd <= segStart) {
            // 极短片段兜底：至少留出 2 个点
            segStart = start + Math.min(0.02, duration * 0.2);
            segEnd = end - Math.min(0.02, duration * 0.2);
        }
        if (segEnd <= segStart) {
            return;
        }

        Map<String, Object> body = buildComicKeyframeBodySingleTrack(draftId, trackName, segStart, segEnd, index, width, height);
        postCapCutJson("/add_video_keyframe", body);
    }

    private Map<String, Object> buildComicKeyframeBodySingleTrack(String draftId,
                                                                  String trackName,
                                                                  double segStart,
                                                                  double segEnd,
                                                                  int index,
                                                                  int width,
                                                                  int height) {
        double duration = Math.max(0.1, segEnd - segStart);
        List<String> propertyTypes = new ArrayList<>();
        List<Double> times = new ArrayList<>();
        List<String> values = new ArrayList<>();

        // 时间点（全部在 segStart~segEnd 内）
        double t0 = segStart;
        double t1 = segStart + duration * 0.22;
        double t2 = segStart + duration * 0.55;
        double t3 = segEnd;
        // 让时间点略微错开，避免不同图片恰好落在同一秒的小数点上（尽量减少重叠）
        double jitter = (index % 7) * 0.003; // 0~0.018s
        t0 += jitter;
        t1 += jitter;
        t2 += jitter;
        t3 += jitter * 0.6;
        if (t3 <= t2) t3 = t2 + 0.05;

        // 每张图使用不同“预设动效”，避免单调
        // 注：单轨道下依然保留尾部复位，避免状态串到下一张
        applyPresetEffect(propertyTypes, times, values, index, t0, t1, t2, t3, width, height);

        Map<String, Object> body = new HashMap<>();
        body.put("draft_id", draftId);
        body.put("track_name", trackName);
        body.put("property_types", propertyTypes);
        body.put("times", times);
        body.put("values", values);
        return body;
    }

    private void addKf(List<String> propertyTypes, List<Double> times, List<String> values,
                       String type, double time, String value) {
        propertyTypes.add(type);
        times.add(time);
        values.add(value);
    }

    /**
     * 预设动效库：按 index 轮换，保证相邻图片动效差异明显。
     * - 仍然遵循：关键帧只落在当前片段时间区间内，末尾复位避免影响下一张。
     */
    private void applyPresetEffect(List<String> propertyTypes,
                                   List<Double> times,
                                   List<String> values,
                                   int index,
                                   double t0, double t1, double t2, double t3,
                                   int width, int height) {
        int dx = Math.max(28, (int) Math.round(width * 0.04));    // 横向 4%
        int dy = Math.max(28, (int) Math.round(height * 0.03));   // 纵向 3%

        // 让不同预设的“力度”有轻微差异
        double strength = 0.85 + (index % 5) * 0.06; // 0.85 ~ 1.09

        // 预设：0~7 循环（8 种），保证每张都不一样（至少在前 8 张内）
        int preset = Math.floorMod(index, 8);

        switch (preset) {
            case 0: {
                // 预设0：左右平移 + 轻微放大（Ken Burns 横向）
                addKf(propertyTypes, times, values, "scale_x", t0, String.valueOf(1.02 * strength));
                addKf(propertyTypes, times, values, "scale_y", t0, String.valueOf(1.02 * strength));
                addKf(propertyTypes, times, values, "position_x_px", t0, String.valueOf(-dx));
                addKf(propertyTypes, times, values, "position_x_px", t2, String.valueOf(dx * 0.4));
                addKf(propertyTypes, times, values, "scale_x", t3, "1.0");
                addKf(propertyTypes, times, values, "scale_y", t3, "1.0");
                addKf(propertyTypes, times, values, "position_x_px", t3, "0");
                break;
            }
            case 1: {
                // 预设1：上下平移 + 反向缩放（从大到小）
                addKf(propertyTypes, times, values, "scale_x", t0, String.valueOf(1.18 * strength));
                addKf(propertyTypes, times, values, "scale_y", t0, String.valueOf(1.18 * strength));
                addKf(propertyTypes, times, values, "position_y_px", t0, String.valueOf(dy));
                addKf(propertyTypes, times, values, "position_y_px", t2, String.valueOf(-dy * 0.6));
                addKf(propertyTypes, times, values, "scale_x", t3, "1.0");
                addKf(propertyTypes, times, values, "scale_y", t3, "1.0");
                addKf(propertyTypes, times, values, "position_y_px", t3, "0");
                break;
            }
            case 2: {
                // 预设2：轻微旋转摇摆 + 轻微位移（更像手持感）
                addKf(propertyTypes, times, values, "rotation", t0, String.valueOf(-3.0 * strength));
                addKf(propertyTypes, times, values, "rotation", t1, String.valueOf(1.2 * strength));
                addKf(propertyTypes, times, values, "rotation", t2, String.valueOf(-1.0 * strength));
                addKf(propertyTypes, times, values, "position_x_px", t0, String.valueOf(-dx * 0.35));
                addKf(propertyTypes, times, values, "position_x_px", t2, String.valueOf(dx * 0.20));
                addKf(propertyTypes, times, values, "rotation", t3, "0.0");
                addKf(propertyTypes, times, values, "position_x_px", t3, "0");
                break;
            }
            case 3: {
                // 预设3：轻微“抖动”三连（位置小幅来回），不做缩放
                int sx = Math.max(10, dx / 6);
                int sy = Math.max(10, dy / 6);
                addKf(propertyTypes, times, values, "position_x_px", t0, String.valueOf(-sx));
                addKf(propertyTypes, times, values, "position_y_px", t0, String.valueOf(sy));
                addKf(propertyTypes, times, values, "position_x_px", t1, String.valueOf(sx));
                addKf(propertyTypes, times, values, "position_y_px", t1, String.valueOf(-sy));
                addKf(propertyTypes, times, values, "position_x_px", t2, String.valueOf(-sx * 0.6));
                addKf(propertyTypes, times, values, "position_y_px", t2, String.valueOf(sy * 0.6));
                addKf(propertyTypes, times, values, "position_x_px", t3, "0");
                addKf(propertyTypes, times, values, "position_y_px", t3, "0");
                break;
            }
            case 4: {
                // 预设4：亮度/对比度渐变（模拟光照变化） + 轻微推镜
                addKf(propertyTypes, times, values, "brightness", t0, String.valueOf(0.05 * strength));
                addKf(propertyTypes, times, values, "contrast", t0, String.valueOf(0.06 * strength));
                addKf(propertyTypes, times, values, "brightness", t2, String.valueOf(-0.03 * strength));
                addKf(propertyTypes, times, values, "contrast", t2, String.valueOf(0.02 * strength));
                addKf(propertyTypes, times, values, "scale_x", t0, String.valueOf(1.06 * strength));
                addKf(propertyTypes, times, values, "scale_y", t0, String.valueOf(1.06 * strength));
                addKf(propertyTypes, times, values, "scale_x", t3, "1.0");
                addKf(propertyTypes, times, values, "scale_y", t3, "1.0");
                // 复位色彩
                addKf(propertyTypes, times, values, "brightness", t3, "0.0");
                addKf(propertyTypes, times, values, "contrast", t3, "0.0");
                break;
            }
            case 5: {
                // 预设5：饱和度渐变（从偏灰到正常）+ 轻微下沉上浮
                addKf(propertyTypes, times, values, "saturation", t0, String.valueOf(-0.35 * strength));
                addKf(propertyTypes, times, values, "saturation", t1, String.valueOf(0.10 * strength));
                addKf(propertyTypes, times, values, "position_y_px", t0, String.valueOf(dy * 0.5));
                addKf(propertyTypes, times, values, "position_y_px", t2, String.valueOf(-dy * 0.2));
                addKf(propertyTypes, times, values, "position_y_px", t3, "0");
                addKf(propertyTypes, times, values, "saturation", t3, "0.0");
                break;
            }
            case 6: {
                // 预设6：轻微“倾斜推进”（旋转 + 缩放 + 横向位移）
                addKf(propertyTypes, times, values, "rotation", t0, String.valueOf(2.4 * strength));
                addKf(propertyTypes, times, values, "scale_x", t0, String.valueOf(1.04 * strength));
                addKf(propertyTypes, times, values, "scale_y", t0, String.valueOf(1.04 * strength));
                addKf(propertyTypes, times, values, "position_x_px", t0, String.valueOf(dx * 0.35));
                addKf(propertyTypes, times, values, "rotation", t2, String.valueOf(-1.2 * strength));
                addKf(propertyTypes, times, values, "position_x_px", t2, String.valueOf(-dx * 0.20));
                addKf(propertyTypes, times, values, "rotation", t3, "0.0");
                addKf(propertyTypes, times, values, "scale_x", t3, "1.0");
                addKf(propertyTypes, times, values, "scale_y", t3, "1.0");
                addKf(propertyTypes, times, values, "position_x_px", t3, "0");
                break;
            }
            default: {
                // 预设7：不做淡入淡出，改做 alpha 小脉冲 + 轻微推镜（节奏感）
                addKf(propertyTypes, times, values, "alpha", t0, "1.0");
                addKf(propertyTypes, times, values, "alpha", t1, String.valueOf(0.78));
                addKf(propertyTypes, times, values, "alpha", t2, "1.0");
                addKf(propertyTypes, times, values, "alpha", t3, "1.0");
                addKf(propertyTypes, times, values, "scale_x", t0, String.valueOf(1.08 * strength));
                addKf(propertyTypes, times, values, "scale_y", t0, String.valueOf(1.08 * strength));
                addKf(propertyTypes, times, values, "scale_x", t2, String.valueOf(1.14 * strength));
                addKf(propertyTypes, times, values, "scale_y", t2, String.valueOf(1.14 * strength));
                addKf(propertyTypes, times, values, "scale_x", t3, "1.0");
                addKf(propertyTypes, times, values, "scale_y", t3, "1.0");
                break;
            }
        }

        // 保底：如果某个预设没有写 alpha，则加一个最轻的淡入淡出（避免突兀）
        boolean hasAlpha = false;
        for (String p : propertyTypes) {
            if ("alpha".equals(p)) {
                hasAlpha = true;
                break;
            }
        }
        if (!hasAlpha) {
            addKf(propertyTypes, times, values, "alpha", t0, "0.0");
            addKf(propertyTypes, times, values, "alpha", t1, "1.0");
            addKf(propertyTypes, times, values, "alpha", t2, "1.0");
            addKf(propertyTypes, times, values, "alpha", t3, "0.0");
        }
    }

    /**
     * 直接调用 CapCut /create_draft，返回 draftId 与 draftUrl（仅供测试用）
     */
    private DraftInfo createDraftInternal(int width, int height) {
        Map<String, Object> body = new HashMap<>();
        body.put("width", width);
        body.put("height", height);

        Map<String, Object> resp = postCapCutJson("/create_draft", body);
        if (resp == null) {
            return null;
        }
        Object success = resp.get("success");
        if (!(success instanceof Boolean) || !((Boolean) success)) {
            log.warn("[TestController] create_draft 调用失败, resp={}", resp);
            return null;
        }
        Object outputObj = resp.get("output");
        if (!(outputObj instanceof Map)) {
            return null;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> output = (Map<String, Object>) outputObj;
        String draftId = output.get("draft_id") != null ? String.valueOf(output.get("draft_id")) : null;
        String draftUrl = output.get("draft_url") != null ? String.valueOf(output.get("draft_url")) : null;

        DraftInfo info = new DraftInfo();
        info.setDraftId(draftId);
        info.setDraftUrl(draftUrl);
        return info;
    }

    /**
     * 直接调用 CapCut /add_image，将图片以「撑满画布」的样式放到 image_main 轨道
     */
    private void addImageInternal(String draftId, String imageUrl, double start, double end, String trackName) {
        Map<String, Object> body = new HashMap<>();
        body.put("draft_id", draftId);
        body.put("image_url", imageUrl);
        body.put("start", start);
        body.put("end", end);
        body.put("track_name", trackName);
        // 必填字段，根据 OpenAPI：transform_y_px
        body.put("transform_y_px", 0);
        // 居中 + 稍微放大，保证基本铺满
        body.put("transform_x", 0.0);
        body.put("transform_y", 0.0);
        body.put("scale_x", 1.1);
        body.put("scale_y", 1.1);

        postCapCutJson("/add_image", body);
    }

    /**
     * 小工具：对 CapCut JSON API 发 POST 请求，只在本测试控制器内部使用
     */
    private Map<String, Object> postCapCutJson(String path, Map<String, Object> body) {
        String url = capcutApiBase + path;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (capcutApiKey != null && !capcutApiKey.isEmpty()) {
            headers.set("Authorization", "Bearer " + capcutApiKey);
        }
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {
                }
        );
        return resp.getBody();
    }

    /**
     * N 张图片生成草稿的请求体
     */
    @Data
    public static class ImagesToDraftRequest {
        /**
         * 图片 URL 列表，按顺序排布在时间轴上
         */
        private List<String> imageUrls;

        /**
         * 整体时长（秒），为空或 <=0 时默认 20s，并均分给每张图片
         */
        private Double totalDurationSeconds;

        /**
         * 画布宽度，默认 1080
         */
        private Integer width;

        /**
         * 画布高度，默认 1920
         */
        private Integer height;
    }

    /**
     * 仅在测试控制器内使用的草稿信息结构，避免复用业务层的 DraftRefOutput
     */
    @Data
    private static class DraftInfo {
        private String draftId;
        private String draftUrl;
    }
}

