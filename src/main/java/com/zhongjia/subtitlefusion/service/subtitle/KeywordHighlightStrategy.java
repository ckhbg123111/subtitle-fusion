package com.zhongjia.subtitlefusion.service.subtitle;

import com.zhongjia.subtitlefusion.model.SubtitleInfo;
import com.zhongjia.subtitlefusion.model.enums.TextStrategyEnum;
import com.zhongjia.subtitlefusion.model.options.KeywordHighlightOptions;
import com.zhongjia.subtitlefusion.model.options.TextRenderRequest;
import io.micrometer.common.util.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Order(0)
public class KeywordHighlightStrategy implements TextRenderStrategy<KeywordHighlightOptions> {

    @Override
    public TextStrategyEnum supports() {
        return TextStrategyEnum.KEYWORD;
    }

    @Override
    public Class<KeywordHighlightOptions> optionsType() {
        return KeywordHighlightOptions.class;
    }

    @Override
    public List<Map<String, Object>> build(TextRenderRequest<KeywordHighlightOptions> req) {
        List<Map<String, Object>> result = new ArrayList<>();

        Map<String, Object> base = new HashMap<>();
        double scaleH = req.getCanvasHeight() > 0 ? Math.min(1.0, (req.getCanvasHeight() / 1280.0)) : 1.0;
        double orientationShrink = (req.getCanvasWidth() > req.getCanvasHeight()) ? 0.5 : 1.0;
        double scale = scaleH * orientationShrink;

        OptionsResolver.Defaults d = new OptionsResolver.Defaults();
        d.baseFontSize = Math.max(3, (int) Math.round(10 * scale));
        d.baseBorderWidth = Math.max(1, (int) Math.round(1 * scale));
        OptionsResolver.Effective eff = OptionsResolver.resolve(req.getStrategyOptions(), d, req.getCanvasWidth(), req.getCanvasHeight());

        base.put("draft_id", req.getDraftId());
        base.put("text", req.getText());
        base.put("start", req.getStart());
        base.put("end", req.getEnd());
        base.put("track_name", "text_fx");
        base.put("font", eff.getFont());
        base.put("font_color", eff.getFontColor());
        base.put("font_size", eff.getFontSize());
        base.put("border_width", eff.getBorderWidth());
        base.put("border_color", eff.getBorderColor());
        base.put("shadow_enabled", eff.isShadowEnabled());
        base.put("shadow_alpha", eff.getShadowAlpha());
        if (eff.getTransformX() != null) base.put("transform_x", eff.getTransformX());
        if (eff.getTransformY() != null) base.put("transform_y", eff.getTransformY());
        if (eff.getIntroAnimation() != null) {
            base.put("intro_animation", eff.getIntroAnimation());
            base.put("intro_duration", eff.getIntroDuration());
        }
        if (eff.getOutroAnimation() != null) {
            base.put("outro_animation", eff.getOutroAnimation());
            base.put("outro_duration", eff.getOutroDuration());
        }

        // 使用富文本子串样式，仅一次 add 即可实现关键词高亮
        List<Map<String, Object>> textStyles = new ArrayList<>();
        String fullText = req.getText();
        List<String> keywords = req.getStrategyOptions() != null ? req.getStrategyOptions().getKeywords() : null;
        if (StringUtils.isNotBlank(fullText) && !CollectionUtils.isEmpty(keywords)) {
            List<int[]> ranges = new ArrayList<>();
            // 收集所有关键词出现位置
            for (String kw : keywords) {
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
                style.put("size", Math.max(3, (int) Math.round(12 * scale)));
                style.put("bold", true);
                style.put("italic", false);
                style.put("underline", false);
                style.put("color", req.getStrategyOptions() != null && req.getStrategyOptions().getKeywordsColor() != null
                        ? req.getStrategyOptions().getKeywordsColor() : "#FFFF00");
                styleEntry.put("style", style);
                // 关键字区间：指定字体与描边
                styleEntry.put("font", req.getStrategyOptions() != null && req.getStrategyOptions().getKeywordsFont() != null
                        ? req.getStrategyOptions().getKeywordsFont() : eff.getFont());
                Map<String, Object> border = new HashMap<>();
                border.put("alpha", 1);
                border.put("color", eff.getBorderColor());
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


