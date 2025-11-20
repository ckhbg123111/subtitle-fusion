package com.zhongjia.subtitlefusion.service.subtitle;

import com.zhongjia.subtitlefusion.model.SubtitleInfo;
import com.zhongjia.subtitlefusion.model.enums.TextStrategyEnum;
import com.zhongjia.subtitlefusion.model.options.StrategyOptions;
import com.zhongjia.subtitlefusion.model.options.TextRenderRequest;

import java.util.List;
import java.util.Map;

public interface TextRenderStrategy<C extends StrategyOptions> {
    TextStrategyEnum supports();

    Class<C> optionsType();

    List<Map<String, Object>> build(TextRenderRequest<C> req);
}


