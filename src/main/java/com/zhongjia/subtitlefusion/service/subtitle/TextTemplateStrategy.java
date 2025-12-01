package com.zhongjia.subtitlefusion.service.subtitle;

import com.zhongjia.subtitlefusion.model.SubtitleInfo;
import com.zhongjia.subtitlefusion.model.SubtitleTemplate;
import com.zhongjia.subtitlefusion.model.enums.TextStrategyEnum;
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
    public TextStrategyEnum supports() {
        return TextStrategyEnum.TEMPLATE;
    }

    @Override
    public Class<TextTemplateOptions> optionsType() {
        return TextTemplateOptions.class;
    }

    @Override
    public java.util.List<TextTemplateOptions> resolveOptions(SubtitleTemplate template) {
        return template != null ? template.getTextTemplateOptions() : null;
    }

    @Override
    public void customizeOption(TextTemplateOptions option, SubtitleTemplate template, SubtitleInfo.SubtitleEffectInfo effectInfo, String fullText) {
        if (effectInfo != null && effectInfo.getTemplateTexts() != null && !effectInfo.getTemplateTexts().isEmpty()) {
            option.setTemplateTexts(effectInfo.getTemplateTexts());
        }
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
            // 调用方未定义模板文案，将采用单行字幕文案
            texts = new ArrayList<>();
            texts.add(req.getText());
        }
        addTpl.put("texts", texts);

        // 缩放（水平/垂直）：未配置时默认 1.0，确保兼容旧行为
        Double sx = (req.getStrategyOptions() != null) ? req.getStrategyOptions().getScaleX() : null;
        Double sy = (req.getStrategyOptions() != null) ? req.getStrategyOptions().getScaleY() : null;
        addTpl.put("scale_x", sx != null ? sx : 1.0);
        addTpl.put("scale_y", sy != null ? sy : 1.0);

        // 位置略微靠下，保持和普通字幕一致的默认位置风格
        Double ty = (req.getStrategyOptions() != null) ? req.getStrategyOptions().getTransformY() : null;
        addTpl.put("transform_y", ty != null ? ty : -0.55);

        list.add(addTpl);
        return list;
    }
}


