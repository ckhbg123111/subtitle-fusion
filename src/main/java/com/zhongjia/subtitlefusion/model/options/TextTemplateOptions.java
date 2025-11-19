package com.zhongjia.subtitlefusion.model.options;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TextTemplateOptions extends StrategyOptions {
    private String templateId;
}


