package com.zhongjia.subtitlefusion.service.subtitle;

import com.zhongjia.subtitlefusion.model.SubtitleInfo;
import com.zhongjia.subtitlefusion.model.options.BasicTextOptions;
import com.zhongjia.subtitlefusion.model.options.TextRenderRequest;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Order(1)
public class BasicTextStrategy implements TextRenderStrategy<BasicTextOptions> {

    @Override
    public boolean supports(SubtitleInfo.CommonSubtitleInfo si) {
        boolean hasKeywords = si != null
                && si.getSubtitleEffectInfo() != null
                && si.getSubtitleEffectInfo().getKeyWords() != null
                && !si.getSubtitleEffectInfo().getKeyWords().isEmpty();
        return !hasKeywords; // 仅在没有关键词时作为处理策略
    }

    @Override
    public Class<BasicTextOptions> optionsType() {
        return BasicTextOptions.class;
    }

    @Override
    public List<Map<String, Object>> build(TextRenderRequest<BasicTextOptions> req) {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> addText = new HashMap<>();
        double scaleH = req.getCanvasHeight() > 0 ? Math.min(1.0, (req.getCanvasHeight() / 1280.0)) : 1.0;
        double orientationShrink = (req.getCanvasWidth() > req.getCanvasHeight()) ? 0.5 : 1.0; // 横屏进一步缩小
        double scale = scaleH * orientationShrink;
        OptionsResolver.Defaults d = new OptionsResolver.Defaults();
        d.baseFontSize = Math.max(3, (int) Math.round(10 * scale));
        d.baseBorderWidth = Math.max(1, (int) Math.round(1 * scale));
        // 解析合并结果
        OptionsResolver.Effective eff = OptionsResolver.resolve(req.getStrategyOptions(), d, req.getCanvasWidth(), req.getCanvasHeight());

        addText.put("draft_id", req.getDraftId());
        addText.put("text", req.getText());
        addText.put("start", req.getStart());
        addText.put("end", req.getEnd());
        addText.put("track_name", "text_fx");
        addText.put("font", eff.getFont());
        addText.put("font_color", eff.getFontColor());
        addText.put("font_size", eff.getFontSize());
        addText.put("border_width", eff.getBorderWidth());
        addText.put("border_color", eff.getBorderColor());
        addText.put("shadow_enabled", eff.isShadowEnabled());
        addText.put("shadow_alpha", eff.getShadowAlpha());
        if (eff.getTransformX() != null) addText.put("transform_x", eff.getTransformX());
        if (eff.getTransformY() != null) addText.put("transform_y", eff.getTransformY());
        if (eff.getIntroAnimation() != null) {
            addText.put("intro_animation", eff.getIntroAnimation());
            addText.put("intro_duration", eff.getIntroDuration());
        }
        if (eff.getOutroAnimation() != null) {
            addText.put("outro_animation", eff.getOutroAnimation());
            addText.put("outro_duration", eff.getOutroDuration());
        }
        list.add(addText);
        return list;
    }
}


