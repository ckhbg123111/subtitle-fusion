package com.zhongjia.subtitlefusion.ffmpeg.ass;

import com.zhongjia.subtitlefusion.model.SubtitleFusionV2Request;

/**
 * 打字机 + 光标闪烁：
 * - 逐字显现：使用可动画剪裁矩形（\clip）配合 \t，在整句时长内从 0 宽推进到全宽；
 * - 光标闪烁：在文本尾部追加“|”，用分段 \t 交替设置 \alpha&H00& / &HFF& 达到闪烁；
 * 不使用淡入，以免与逐显效果冲突。
 */
public class TypewriterCursorStrategy implements AssEffectStrategy {
    @Override
    public String buildOverrideTags(SubtitleFusionV2Request.CommonSubtitleInfo lineInfo, int playX, int playY) {
        int durationMs = Math.max(300, computeDurationMs(lineInfo));
        StringBuilder sb = new StringBuilder();
        // 初始完全裁切：clip(0,0,0,playY)，随后在整句时长内推进到 clip(0,0,playX,playY)
        sb.append("\\clip(0,0,0,").append(playY).append(")");
        sb.append("\\t(0,").append(durationMs).append(",\\clip(0,0,")
          .append(playX).append(',').append(playY).append(") )");
        return sb.toString();
    }

    @Override
    public String rewriteTextWithKeywords(SubtitleFusionV2Request.CommonSubtitleInfo lineInfo) {
        String base = lineInfo != null && lineInfo.getText() != null ? lineInfo.getText() : "";
        int durationMs = Math.max(300, computeDurationMs(lineInfo));
        // 光标闪烁：约 300ms 一次，贯穿整句时长
        int period = 300;
        int cycles = Math.max(1, Math.min(20, durationMs / period + 1));
        StringBuilder blink = new StringBuilder();
        blink.append("{\\bord0\\shad0\\b1\\alpha&HFF&"); // 初始透明
        int t0 = 0;
        for (int i = 0; i < cycles; i++) {
            int mid = Math.min(durationMs, t0 + period / 2);
            int t1 = Math.min(durationMs, t0 + period);
            // 前半段显现（可见），后半段隐藏（透明）
            blink.append("\\t(").append(t0).append(",").append(mid).append(",\\alpha&H00&)");
            blink.append("\\t(").append(mid).append(",").append(t1).append(",\\alpha&HFF&)");
            t0 += period;
            if (t0 >= durationMs) break;
        }
        blink.append("}");
        return base + blink + "|";
    }

    private int computeDurationMs(SubtitleFusionV2Request.CommonSubtitleInfo lineInfo) {
        try {
            int s = parseTimeToMs(lineInfo != null ? lineInfo.getStartTime() : null);
            int e = parseTimeToMs(lineInfo != null ? lineInfo.getEndTime() : null);
            int d = e - s;
            return d > 0 ? d : 800;
        } catch (Exception ignore) {
            return 800;
        }
    }

    private int parseTimeToMs(String t) {
        if (t == null || t.trim().isEmpty()) return 0;
        String s = t.trim().replace(',', '.');
        if (s.matches("^\\d+(\\.\\d+)?$")) {
            double sec = Double.parseDouble(s);
            return (int) Math.round(sec * 1000.0);
        }
        String[] parts = s.split(":");
        if (parts.length < 3) return 0;
        int h = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[1]);
        double sec = Double.parseDouble(parts[2]);
        return (int) Math.round(((h * 3600) + (m * 60) + sec) * 1000.0);
    }
}


