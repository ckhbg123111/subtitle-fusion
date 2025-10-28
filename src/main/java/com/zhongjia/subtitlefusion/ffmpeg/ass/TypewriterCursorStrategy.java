package com.zhongjia.subtitlefusion.ffmpeg.ass;

import com.zhongjia.subtitlefusion.model.SubtitleFusionV2Request;

/**
 * 打字机效果：使用可动画的剪裁矩形（\clip）配合 \t 在整句时长内从 0 宽度推进到全宽，
 * 达到从左到右逐步显现的效果；不使用淡入。
 */
public class TypewriterCursorStrategy implements AssEffectStrategy {
    @Override
    public String buildOverrideTags(SubtitleFusionV2Request.CommonSubtitleInfo lineInfo, int playX, int playY) {
		int durationMs = Math.max(300, computeDurationMs(lineInfo));
		// 以全屏宽度作为剪裁推进范围，确保任何对齐方式下都能产生左->右显现
		// 初始：clip(0,0,0,playY) 完全不可见；结束：clip(0,0,playX,playY) 全部可见
		StringBuilder sb = new StringBuilder();
		sb.append("\\clip(")
		  .append(0).append(',').append(0).append(',').append(0).append(',').append(playY)
		  .append(')');
		sb.append("\\t(0,").append(durationMs).append(",\\clip(")
		  .append(0).append(',').append(0).append(',').append(playX).append(',').append(playY)
		  .append(") )");
		return sb.toString();
    }

	@Override
	public String rewriteTextWithKeywords(SubtitleFusionV2Request.CommonSubtitleInfo lineInfo) {
		String base = lineInfo != null && lineInfo.getText() != null ? lineInfo.getText() : "";
		int durationMs = Math.max(300, computeDurationMs(lineInfo));
		// 构造光标闪烁：在整句时长内以约 300ms 为周期切换 alpha（可见/不可见）
		int period = 300;
		int cycles = Math.max(1, Math.min(12, durationMs / period));
		StringBuilder blink = new StringBuilder();
		blink.append("{\\bord0\\shad0\\b1\\alpha&H00&");
		int t0 = 0;
		for (int i = 0; i < cycles; i++) {
			int mid = t0 + period / 2;
			int t1 = Math.min(durationMs, t0 + period);
			// 前半段显现到不透明，后半段淡出到全透明
			blink.append("\\t(").append(t0).append(",").append(mid).append(",\\alpha&H00&)");
			blink.append("\\t(").append(mid).append(",").append(t1).append(",\\alpha&HFF&)");
			t0 += period;
			if (t0 >= durationMs) break;
		}
		blink.append("}");
		String cursorChar = "|";
		return base + blink + cursorChar;
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
			// 纯秒
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


