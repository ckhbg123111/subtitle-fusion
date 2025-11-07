package com.zhongjia.subtitlefusion.service.subtitle;

import com.zhongjia.subtitlefusion.model.SubtitleFusionV2Request;
import com.zhongjia.subtitlefusion.util.ColorUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Order(0)
public class KeywordHighlightStrategy implements TextRenderStrategy {

    @Override
    public boolean supports(SubtitleFusionV2Request.CommonSubtitleInfo si) {
        return si != null
                && si.getSubtitleEffectInfo() != null
                && si.getSubtitleEffectInfo().getKeyWords() != null
                && !si.getSubtitleEffectInfo().getKeyWords().isEmpty();
    }

    @Override
    public List<Map<String, Object>> build(String draftId, SubtitleFusionV2Request.CommonSubtitleInfo si, double start, double end, String textIntro, String textOutro) {
        List<Map<String, Object>> result = new ArrayList<>();

        Map<String, Object> base = new HashMap<>();
        base.put("draft_id", draftId);
        base.put("text", si.getText());
        base.put("start", start);
        base.put("end", end);
        base.put("track_name", "text_fx");
        base.put("font", "SourceHanSansCN_Regular");
        base.put("font_color", "#FFFFFF");
        base.put("font_size", 6);
        base.put("border_width", 1);
        base.put("border_color", "#000000");
        base.put("shadow_enabled", true);
        base.put("shadow_alpha", 0.8);
        base.put("transform_y", -0.8);
        result.add(base);

        for (String kw : si.getSubtitleEffectInfo().getKeyWords()) {
            if (kw == null || kw.isEmpty()) continue;
            Map<String, Object> fancy = new HashMap<>();
            fancy.put("draft_id", draftId);
            fancy.put("text", kw);
            fancy.put("start", start);
            fancy.put("end", end);
            fancy.put("track_name", "text_fx");
            fancy.put("font", "文轩体");
            fancy.put("font_color", ColorUtils.randomBrightColor());
            fancy.put("font_size", 9);
            fancy.put("border_width", 1);
            fancy.put("border_color", "#8A2BE2");
            fancy.put("shadow_enabled", true);
            fancy.put("shadow_alpha", 0.9);
            double dy = -0.74 + ThreadLocalRandom.current().nextDouble(0.0, 0.08);
            double dx = -0.15 + ThreadLocalRandom.current().nextDouble(0.0, 0.30);
            fancy.put("transform_y", dy);
            fancy.put("transform_x", dx);
            result.add(fancy);
        }

        return result;
    }
}


