package com.zhongjia.subtitlefusion.service.subtitle;

import com.zhongjia.subtitlefusion.model.SubtitleInfo;
import com.zhongjia.subtitlefusion.model.options.TextRenderRequest;
import com.zhongjia.subtitlefusion.model.options.TextTemplateOptions;
import io.micrometer.common.util.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

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
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> addTpl = new HashMap<>();
        addTpl.put("draft_id", req.getDraftId());
        if (req.getStrategyOptions() != null && StringUtils.isNotBlank(req.getStrategyOptions().getTemplateId())) {
            addTpl.put("template_id", req.getStrategyOptions().getTemplateId());
        }
        addTpl.put("start", req.getStart());
        addTpl.put("end", req.getEnd());
        addTpl.put("track_name", "text_template");

        List<String> texts = req.getStrategyOptions() == null ? null : req.getStrategyOptions().getTemplateTexts();
        if (CollectionUtils.isEmpty(texts)) {
            texts = new ArrayList<>();
            texts.add(req.getText());
        }
        addTpl.put("texts", texts);

        // 位置略微靠下，保持和普通字幕一致的默认位置风格
        Double ty = (req.getStrategyOptions() != null) ? req.getStrategyOptions().getTransformY() : null;
        addTpl.put("transform_y", ty != null ? ty : -0.55);

        list.add(addTpl);
        return list;
    }
}


