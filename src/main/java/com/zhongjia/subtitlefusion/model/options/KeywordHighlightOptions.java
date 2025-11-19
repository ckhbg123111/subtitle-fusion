package com.zhongjia.subtitlefusion.model.options;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class KeywordHighlightOptions extends StrategyOptions {
    private List<String> keywords;
    private String keywordsColor;
    private String keywordsFont;
}


