package com.zhongjia.subtitlefusion.model;

import com.zhongjia.subtitlefusion.model.options.BasicTextOptions;
import com.zhongjia.subtitlefusion.model.options.FlowerTextOptions;
import com.zhongjia.subtitlefusion.model.options.KeywordHighlightOptions;
import com.zhongjia.subtitlefusion.model.options.TextTemplateOptions;
import lombok.Data;

import java.util.List;

@Data
public class SubtitleTemplate {
    private List<FlowerTextOptions>  flowerTextOptions;
    private List<TextTemplateOptions>  textTemplateOptions;
    private List<KeywordHighlightOptions>  keywordHighlightOptions;
    private List<BasicTextOptions>  basicTextOptions;
}
