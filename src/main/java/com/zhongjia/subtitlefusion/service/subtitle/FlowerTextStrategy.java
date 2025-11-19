package com.zhongjia.subtitlefusion.service.subtitle;

import com.zhongjia.subtitlefusion.model.SubtitleInfo;
import com.zhongjia.subtitlefusion.model.options.FlowerTextOptions;
import com.zhongjia.subtitlefusion.model.options.TextRenderRequest;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Order(-1)
public class FlowerTextStrategy implements TextRenderStrategy<FlowerTextOptions> {

    @Override
    public boolean supports(SubtitleInfo.CommonSubtitleInfo si) {
        return si != null
                && si.getSubtitleEffectInfo() != null
                && si.getSubtitleEffectInfo().getTextEffectId() != null
                && !si.getSubtitleEffectInfo().getTextEffectId().isEmpty();
    }

    @Override
    public Class<FlowerTextOptions> optionsType() {
        return FlowerTextOptions.class;
    }

    @Override
    public List<Map<String, Object>> build(TextRenderRequest<FlowerTextOptions> req) {
        SubtitleInfo.CommonSubtitleInfo si = req.getSubtitle();
        List<Map<String, Object>> list = new ArrayList<>();

        Map<String, Object> addText = new HashMap<>();
        double scaleH = req.getCanvasHeight() > 0 ? Math.min(1.0, (req.getCanvasHeight() / 1280.0)) : 1.0;
        double orientationShrink = (req.getCanvasWidth() > req.getCanvasHeight()) ? 0.5 : 1.0;
        double scale = scaleH * orientationShrink;
        OptionsResolver.Defaults d = new OptionsResolver.Defaults();
        d.baseFontSize = Math.max(4, (int) Math.round(10 * scale));
        d.baseBorderWidth = Math.max(1, (int) Math.round(1 * scale));
        OptionsResolver.Effective eff = OptionsResolver.resolve(req.getStrategyOptions(), d, req.getCanvasWidth(), req.getCanvasHeight());

        addText.put("draft_id", req.getDraftId());
        addText.put("text", si.getText());
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

        // 花字效果ID
        String effectId = null;
        if (req.getStrategyOptions() != null && req.getStrategyOptions().getEffectId() != null && !req.getStrategyOptions().getEffectId().isEmpty()) {
            effectId = req.getStrategyOptions().getEffectId();
        } else if (si.getSubtitleEffectInfo() != null) {
            effectId = si.getSubtitleEffectInfo().getTextEffectId();
        }
        if (effectId != null && !effectId.isEmpty()) {
            addText.put("effect_effect_id", effectId);
        }

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
 

