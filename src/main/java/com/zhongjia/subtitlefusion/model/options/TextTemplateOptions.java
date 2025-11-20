package com.zhongjia.subtitlefusion.model.options;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class TextTemplateOptions extends StrategyOptions {
    private String templateId;
    private List<String> templateTexts;
}


