package com.zhongjia.subtitlefusion.service.subtitle;

import com.zhongjia.subtitlefusion.model.SubtitleInfo;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Order(1)
public class BasicTextStrategy implements TextRenderStrategy {

    @Override
    public boolean supports(SubtitleInfo.CommonSubtitleInfo si) {
        boolean hasKeywords = si != null
                && si.getSubtitleEffectInfo() != null
                && si.getSubtitleEffectInfo().getKeyWords() != null
                && !si.getSubtitleEffectInfo().getKeyWords().isEmpty();
        return !hasKeywords; // 仅在没有关键词时作为处理策略
    }

    @Override
    public List<Map<String, Object>> build(String draftId, SubtitleInfo.CommonSubtitleInfo si, double start, double end, String textIntro, String textOutro, int canvasWidth, int canvasHeight) {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> addText = new HashMap<>();
        double scale = canvasHeight > 0 ? (canvasHeight / 1280.0) : 1.0;
        int fontSize = Math.max(5, (int) Math.round(8 * scale));
        int borderWidth = Math.max(1, (int) Math.round(1 * scale));
        addText.put("draft_id", draftId);
        addText.put("text", si.getText());
        addText.put("start", start);
        addText.put("end", end);
        addText.put("track_name", "text_fx");
        addText.put("font", "SourceHanSansCN_Regular");
        addText.put("font_color", "#FFFFFF");
        addText.put("font_size", fontSize);
        addText.put("border_width", borderWidth);
        addText.put("border_color", "#000000");
        addText.put("shadow_enabled", true);
        addText.put("shadow_alpha", 0.8);
        addText.put("transform_y", -0.75);
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


