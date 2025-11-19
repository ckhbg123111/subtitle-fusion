package com.zhongjia.subtitlefusion.model.options;

import lombok.Data;

import java.util.List;

@Data
public class KeywordHighlightOptions implements StrategyOptions {
    private List<String> keywords;
    private String keywordsColor;
    private String keywordsFont;
}


