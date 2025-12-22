package com.zhongjia.subtitlefusion.service.webtoon;

import com.zhongjia.subtitlefusion.model.WebtoonDramaKeyframeSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 构建“漫剧”图片关键帧（复用 TestController 的成熟思路，但固化为 3 个默认预设）。
 */
public class WebtoonDramaKeyframeBuilder {

    public static final String PRESET_P0_PAN_ZOOM = "P0_PAN_ZOOM";
    public static final String PRESET_P1_VERTICAL_ZOOM_OUT = "P1_VERTICAL_ZOOM_OUT";
    public static final String PRESET_P2_ROTATE_SHAKE = "P2_ROTATE_SHAKE";

    /**
     * 生成 /add_video_keyframe 请求体。
     *
     * @param spec         可为空；为空时按 segmentIndex%3 兜底
     * @param segmentIndex 段索引，用于轮换预设与 jitter
     */
    public Map<String, Object> buildKeyframeBody(String draftId,
                                                 String trackName,
                                                 double start,
                                                 double end,
                                                 int segmentIndex,
                                                 int width,
                                                 int height,
                                                 WebtoonDramaKeyframeSpec spec) {
        double duration = Math.max(0.1, end - start);

        // 关键帧边界内缩：避免与相邻片段边界重合导致状态串扰
        double eps = Math.min(0.08, duration * 0.12);
        eps = Math.min(eps, duration * 0.45);
        double segStart = start + eps;
        double segEnd = end - eps;
        if (segEnd <= segStart) {
            segStart = start + Math.min(0.02, duration * 0.2);
            segEnd = end - Math.min(0.02, duration * 0.2);
        }
        if (segEnd <= segStart) {
            return null;
        }

        String preset = choosePreset(segmentIndex, spec);
        double strength = (spec != null && spec.getStrength() != null && spec.getStrength() > 0) ? spec.getStrength() : 1.0;

        // 时间点（全部在 segStart~segEnd 内）
        double d = Math.max(0.1, segEnd - segStart);
        double t0 = segStart;
        double t1 = segStart + d * 0.22;
        double t2 = segStart + d * 0.55;
        double t3 = segEnd;

        double baseJitter = (segmentIndex % 7) * 0.003; // 0~0.018s
        double extra = (spec != null && spec.getJitterSec() != null) ? spec.getJitterSec() : 0.0;
        double jitter = baseJitter + extra;
        t0 += jitter;
        t1 += jitter;
        t2 += jitter;
        t3 += jitter * 0.6;
        if (t3 <= t2) t3 = t2 + 0.05;

        int dx = Math.max(28, (int) Math.round(width * 0.04));   // 横向 4%
        int dy = Math.max(28, (int) Math.round(height * 0.03));  // 纵向 3%

        List<String> propertyTypes = new ArrayList<>();
        List<Double> times = new ArrayList<>();
        List<String> values = new ArrayList<>();

        switch (preset) {
            case PRESET_P1_VERTICAL_ZOOM_OUT: {
                // 从大到小 + 轻微上下移动
                addKf(propertyTypes, times, values, "scale_x", t0, String.valueOf(1.18 * strength));
                addKf(propertyTypes, times, values, "scale_y", t0, String.valueOf(1.18 * strength));
                addKf(propertyTypes, times, values, "position_y_px", t0, String.valueOf(dy));
                addKf(propertyTypes, times, values, "position_y_px", t2, String.valueOf(-dy * 0.6));
                // 末尾复位
                addKf(propertyTypes, times, values, "scale_x", t3, "1.0");
                addKf(propertyTypes, times, values, "scale_y", t3, "1.0");
                addKf(propertyTypes, times, values, "position_y_px", t3, "0");
                break;
            }
            case PRESET_P2_ROTATE_SHAKE: {
                // 轻微旋转摇摆 + 横向位移
                addKf(propertyTypes, times, values, "rotation", t0, String.valueOf(-3.0 * strength));
                addKf(propertyTypes, times, values, "rotation", t1, String.valueOf(1.2 * strength));
                addKf(propertyTypes, times, values, "rotation", t2, String.valueOf(-1.0 * strength));
                addKf(propertyTypes, times, values, "position_x_px", t0, String.valueOf(-dx * 0.35));
                addKf(propertyTypes, times, values, "position_x_px", t2, String.valueOf(dx * 0.20));
                // 末尾复位
                addKf(propertyTypes, times, values, "rotation", t3, "0.0");
                addKf(propertyTypes, times, values, "position_x_px", t3, "0");
                break;
            }
            case PRESET_P0_PAN_ZOOM:
            default: {
                // 左右平移 + 轻微放大（Ken Burns 横向）
                addKf(propertyTypes, times, values, "scale_x", t0, String.valueOf(1.02 * strength));
                addKf(propertyTypes, times, values, "scale_y", t0, String.valueOf(1.02 * strength));
                addKf(propertyTypes, times, values, "position_x_px", t0, String.valueOf(-dx));
                addKf(propertyTypes, times, values, "position_x_px", t2, String.valueOf(dx * 0.4));
                // 末尾复位
                addKf(propertyTypes, times, values, "scale_x", t3, "1.0");
                addKf(propertyTypes, times, values, "scale_y", t3, "1.0");
                addKf(propertyTypes, times, values, "position_x_px", t3, "0");
                break;
            }
        }

        // 保底：若预设未写 alpha，则加最轻淡入淡出避免突兀
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

        Map<String, Object> body = new HashMap<>();
        body.put("draft_id", draftId);
        body.put("track_name", trackName);
        body.put("property_types", propertyTypes);
        body.put("times", times);
        body.put("values", values);
        return body;
    }

    private String choosePreset(int segmentIndex, WebtoonDramaKeyframeSpec spec) {
        if (spec != null && spec.getPreset() != null && !spec.getPreset().trim().isEmpty()) {
            return spec.getPreset().trim();
        }
        int k = Math.floorMod(segmentIndex, 3);
        if (k == 1) return PRESET_P1_VERTICAL_ZOOM_OUT;
        if (k == 2) return PRESET_P2_ROTATE_SHAKE;
        return PRESET_P0_PAN_ZOOM;
    }

    private void addKf(List<String> propertyTypes, List<Double> times, List<String> values,
                       String type, double time, String value) {
        propertyTypes.add(type);
        times.add(time);
        values.add(value);
    }
}


