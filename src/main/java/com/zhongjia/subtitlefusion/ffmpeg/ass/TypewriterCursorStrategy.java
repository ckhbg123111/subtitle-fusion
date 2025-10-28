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
        // 使用逐字离散显现，事件级不再使用连续裁剪/淡入
        return "";
    }

    @Override
    public String rewriteTextWithKeywords(SubtitleFusionV2Request.CommonSubtitleInfo lineInfo) {
        String text = lineInfo != null && lineInfo.getText() != null ? lineInfo.getText() : "";
        int durationMs = Math.max(300, computeDurationMs(lineInfo));

        // 拆分为 Unicode 码点，逐字离散显现
        java.util.List<String> glyphs = new java.util.ArrayList<>();
        for (int i = 0; i < text.length(); ) {
            int cp = Character.codePointAt(text, i);
            glyphs.add(new String(Character.toChars(cp)));
            i += Character.charCount(cp);
        }
        int n = glyphs.size();
        if (n == 0) return "";

        // 每个字的时间步长，保证离散；为避免过快/过慢设上下界
        int minPer = 50;   // 最小 50ms 一字
        int maxPer = 220;  // 最大 220ms 一字
        int per = Math.max(minPer, Math.min(maxPer, Math.max(1, durationMs / Math.max(1, n))));
        int revealStep = Math.min(30, Math.max(1, per / 6)); // 切换用极短过渡
        int startOffset = 20; // 小偏移，避免首字在 t=0 渲染差异

        // 光标在当前字与下一个字之间的时间窗内可见；窗口内做轻微闪烁
        int blinkPeriod = 280; // 光标闪烁周期

        StringBuilder out = new StringBuilder();
        for (int i = 0; i < n; i++) {
            int tStart = startOffset + i * per;
            // 当前字：起始不可见，在 tStart -> tStart+revealStep 迅速显现
            out.append("{\\alpha&HFF&\\t(")
               .append(tStart).append(',').append(tStart + revealStep)
               .append(",\\alpha&H00&)}")
               .append(glyphs.get(i));

            // 插入随字移动的光标“|”：仅在 [tStart, tEnd) 时间窗内存在
            int tEnd = (i < n - 1) ? (startOffset + (i + 1) * per) : Math.min(durationMs, tStart + Math.max(per, 600));
            if (tEnd > tStart) {
                out.append(buildCursorSegment(tStart, tEnd, blinkPeriod));
            }
        }
        return out.toString();
    }

    private String buildCursorSegment(int tStart, int tEnd, int blinkPeriod) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\\bord0\\shad0\\b1\\alpha&HFF&"); // 初始透明
        int t = tStart;
        while (t < tEnd) {
            int mid = Math.min(tEnd, t + blinkPeriod / 2);
            int next = Math.min(tEnd, t + blinkPeriod);
            sb.append("\\t(").append(t).append(',').append(mid).append(",\\alpha&H00&)");
            sb.append("\\t(").append(mid).append(',').append(next).append(",\\alpha&HFF&)");
            t = next;
        }
        sb.append('}').append('|').append("{\\r}");
        return sb.toString();
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


