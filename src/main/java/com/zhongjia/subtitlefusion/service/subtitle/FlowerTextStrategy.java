package com.zhongjia.subtitlefusion.service.subtitle;

import com.zhongjia.subtitlefusion.model.SubtitleFusionV2Request;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Order(-1)
public class FlowerTextStrategy implements TextRenderStrategy {

    @Override
    public boolean supports(SubtitleFusionV2Request.CommonSubtitleInfo si) {
        return si != null
                && si.getSubtitleEffectInfo() != null
                && si.getSubtitleEffectInfo().getTextEffectId() != null
                && !si.getSubtitleEffectInfo().getTextEffectId().isEmpty();
    }

    @Override
    public List<Map<String, Object>> build(String draftId,
                                           SubtitleFusionV2Request.CommonSubtitleInfo si,
                                           double start,
                                           double end,
                                           String textIntro,
                                           String textOutro) {
        List<Map<String, Object>> list = new ArrayList<>();

        Map<String, Object> addText = new HashMap<>();
        addText.put("draft_id", draftId);
        addText.put("text", si.getText());
        addText.put("start", start);
        addText.put("end", end);
        addText.put("track_name", "text_fx");
        addText.put("font", "SourceHanSansCN_Regular");
        addText.put("font_color", "#FFFFFF");
        addText.put("font_size", 16);
        addText.put("border_width", 1);
        addText.put("border_color", "#000000");
        addText.put("shadow_enabled", true);
        addText.put("shadow_alpha", 0.8);
        addText.put("transform_y", 0.75);

        // 花字效果ID
        addText.put("effect_effect_id", si.getSubtitleEffectInfo().getTextEffectId());

        if (textIntro != null) {
            addText.put("intro_animation", textIntro);
            addText.put("intro_duration", 0.5);
        }
        if (textOutro != null) {
            addText.put("outro_animation", textOutro);
            addText.put("outro_duration", 0.5);
        }
        list.add(addText);
        return list;
    }
}


