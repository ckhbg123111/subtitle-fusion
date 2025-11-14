package com.zhongjia.subtitlefusion.service.subtitle;

import com.zhongjia.subtitlefusion.model.SubtitleFusionV2Request;
import com.zhongjia.subtitlefusion.model.SubtitleInfo;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Order(-2)
public class TextTemplateStrategy implements TextRenderStrategy {

    @Override
    public boolean supports(SubtitleInfo.CommonSubtitleInfo si) {
        return si != null
                && si.getSubtitleEffectInfo() != null
                && si.getSubtitleEffectInfo().getTextTemplateId() != null
                && !si.getSubtitleEffectInfo().getTextTemplateId().isEmpty();
    }

    @Override
    public List<Map<String, Object>> build(String draftId,
                                           SubtitleInfo.CommonSubtitleInfo si,
                                           double start,
                                           double end,
                                           String textIntro,
                                           String textOutro) {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> addTpl = new HashMap<>();
        addTpl.put("draft_id", draftId);
        addTpl.put("template_id", si.getSubtitleEffectInfo().getTextTemplateId());
        addTpl.put("start", start);
        addTpl.put("end", end);
        addTpl.put("track_name", "text_template");

        List<String> texts = si.getSubtitleEffectInfo().getTemplateTexts();
        if (texts == null || texts.isEmpty()) {
            texts = new ArrayList<>();
            texts.add(si.getText());
        }
        addTpl.put("texts", texts);

        // 位置略微靠下，保持和普通字幕一致的默认位置风格
        addTpl.put("transform_y", 0.75);

        list.add(addTpl);
        return list;
    }
}


