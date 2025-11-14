package com.zhongjia.subtitlefusion.service.subtitle;

import com.zhongjia.subtitlefusion.model.SubtitleInfo;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
 

@Component
@Order(0)
public class KeywordHighlightStrategy implements TextRenderStrategy {

    @Override
    public boolean supports(SubtitleInfo.CommonSubtitleInfo si) {
        return si != null
                && si.getSubtitleEffectInfo() != null
                && si.getSubtitleEffectInfo().getKeyWords() != null
                && !si.getSubtitleEffectInfo().getKeyWords().isEmpty();
    }

    @Override
    public List<Map<String, Object>> build(String draftId, SubtitleInfo.CommonSubtitleInfo si, double start, double end, String textIntro, String textOutro, int canvasWidth, int canvasHeight) {
        List<Map<String, Object>> result = new ArrayList<>();

        Map<String, Object> base = new HashMap<>();
        double scale = canvasHeight > 0 ? Math.min(1.0, (canvasHeight / 1280.0)) : 1.0;
        int baseFontSize = Math.max(5, (int) Math.round(12 * scale));
        int baseBorderWidth = Math.max(1, (int) Math.round(1 * scale));
        base.put("draft_id", draftId);
        base.put("text", si.getText());
        base.put("start", start);
        base.put("end", end);
        base.put("track_name", "text_fx");
        base.put("font", "匹喏曹");
        base.put("font_color", "#FFFFFF");
        base.put("font_size", baseFontSize);
        base.put("border_width", baseBorderWidth);
        base.put("border_color", "#000000");
        base.put("shadow_enabled", true);
        base.put("shadow_alpha", 0.8);
        base.put("transform_y", -0.75);
		if (textIntro != null) {
			base.put("intro_animation", textIntro);
			base.put("intro_duration", 0.5);
		}
		if (textOutro != null) {
			base.put("outro_animation", textOutro);
			base.put("outro_duration", 0.5);
		}

		// 使用富文本子串样式，仅一次 add 即可实现关键词高亮
		List<Map<String, Object>> textStyles = new ArrayList<>();
		String fullText = si.getText();
		if (fullText != null && !fullText.isEmpty()) {
			List<int[]> ranges = new ArrayList<>();
			// 收集所有关键词出现位置
			for (String kw : si.getSubtitleEffectInfo().getKeyWords()) {
				if (kw == null || kw.isEmpty()) continue;
				int from = 0;
				while (from < fullText.length()) {
					int idx = fullText.indexOf(kw, from);
					if (idx < 0) break;
					ranges.add(new int[]{idx, idx + kw.length()});
					from = idx + kw.length();
				}
			}
			// 去重并处理重叠：按起点排序，跳过与上一个已接受区间重叠的区间
			ranges.sort((a, b) -> Integer.compare(a[0], b[0]));
			List<int[]> nonOverlap = new ArrayList<>();
			int lastEnd = -1;
			for (int[] r : ranges) {
				if (r[0] >= lastEnd) {
					nonOverlap.add(r);
					lastEnd = r[1];
				}
			}
			for (int[] r : nonOverlap) {
				Map<String, Object> styleEntry = new HashMap<>();
				styleEntry.put("start", r[0]);
				styleEntry.put("end", r[1]);
				Map<String, Object> style = new HashMap<>();
                style.put("size", Math.max(6, (int) Math.round(15 * scale)));
				style.put("bold", true);
				style.put("italic", false);
				style.put("underline", false);
				style.put("color", "#FFFF00");
				styleEntry.put("style", style);
				// 关键字区间：指定字体与描边
				styleEntry.put("font", "匹喏曹");
				Map<String, Object> border = new HashMap<>();
				border.put("alpha", 1);
				border.put("color", "#000000");
				border.put("width", Math.max(1, (int) Math.round(2 * scale)));
				styleEntry.put("border", border);
				textStyles.add(styleEntry);
			}
		}
		if (!textStyles.isEmpty()) {
			base.put("text_styles", textStyles);
		}

		result.add(base);

        return result;
    }
}


