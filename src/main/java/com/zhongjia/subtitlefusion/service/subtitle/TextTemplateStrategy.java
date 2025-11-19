package com.zhongjia.subtitlefusion.service.subtitle;

import com.zhongjia.subtitlefusion.model.SubtitleInfo;
import com.zhongjia.subtitlefusion.model.options.TextRenderRequest;
import com.zhongjia.subtitlefusion.model.options.TextTemplateOptions;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Order(-2)
public class TextTemplateStrategy implements TextRenderStrategy<TextTemplateOptions> {

    @Override
    public boolean supports(SubtitleInfo.CommonSubtitleInfo si) {
        return si != null
                && si.getSubtitleEffectInfo() != null
                && si.getSubtitleEffectInfo().getTextTemplateId() != null
                && !si.getSubtitleEffectInfo().getTextTemplateId().isEmpty();
    }

    @Override
    public Class<TextTemplateOptions> optionsType() {
        return TextTemplateOptions.class;
    }

    @Override
    public List<Map<String, Object>> build(TextRenderRequest<TextTemplateOptions> req) {
        SubtitleInfo.CommonSubtitleInfo si = req.getSubtitle();
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> addTpl = new HashMap<>();
        addTpl.put("draft_id", req.getDraftId());

        String templateId = null;
        if (req.getStrategyOptions() != null && req.getStrategyOptions().getTemplateId() != null && !req.getStrategyOptions().getTemplateId().isEmpty()) {
            templateId = req.getStrategyOptions().getTemplateId();
        } else if (si.getSubtitleEffectInfo() != null) {
            templateId = si.getSubtitleEffectInfo().getTextTemplateId();
        }
        if (templateId != null && !templateId.isEmpty()) {
            addTpl.put("template_id", templateId);
        }
        addTpl.put("start", req.getStart());
        addTpl.put("end", req.getEnd());
        addTpl.put("track_name", "text_template");

        List<String> texts = si.getSubtitleEffectInfo() != null ? si.getSubtitleEffectInfo().getTemplateTexts() : null;
        if (texts == null || texts.isEmpty()) {
            texts = new ArrayList<>();
            texts.add(si.getText());
        }
        addTpl.put("texts", texts);

        // 位置略微靠下，保持和普通字幕一致的默认位置风格
        Double ty = req.getCommon() != null ? req.getCommon().getTransformY() : null;
        addTpl.put("transform_y", ty != null ? ty : -0.55);

        list.add(addTpl);
        return list;
    }
}


