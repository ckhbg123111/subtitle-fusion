package com.zhongjia.subtitlefusion.service.subtitle;

import com.zhongjia.subtitlefusion.model.CommonSubtitleInfo;
import com.zhongjia.subtitlefusion.model.SubtitleInfo;
import com.zhongjia.subtitlefusion.model.SubtitleTemplate;
import com.zhongjia.subtitlefusion.model.enums.TextStrategyEnum;
import com.zhongjia.subtitlefusion.model.options.StrategyOptions;
import com.zhongjia.subtitlefusion.model.options.TextRenderRequest;
import com.zhongjia.subtitlefusion.util.RandomUtils;

import java.util.List;
import java.util.Map;
import java.util.Collections;

public interface TextRenderStrategy<C extends StrategyOptions> {
    TextStrategyEnum supports();

    Class<C> optionsType();

    List<Map<String, Object>> build(TextRenderRequest<C> req);

    /**
     * 从模板中解析出当前策略对应的可选项（通常直接返回模板上的该类 options 列表）
     */
    List<C> resolveOptions(SubtitleTemplate template);

    /**
     * 按需基于外部上下文二次定制选中的 option（默认无定制）
     */
    default void customizeOption(C option, SubtitleTemplate template, CommonSubtitleInfo.SubtitleEffectInfo effectInfo, String fullText) {}

    /**
     * 统一流程：从模板解析 -> 随机挑选 -> 结合上下文定制 -> 构建 payload
     */
    default List<Map<String, Object>> buildWithAutoOptions(String draftId,
                                                           String text,
                                                           double start,
                                                           double end,
                                                           int canvasWidth,
                                                           int canvasHeight,
                                                           SubtitleTemplate template,
                                                           CommonSubtitleInfo.SubtitleEffectInfo effectInfo) {
        List<C> options = resolveOptions(template);
        if (options == null || options.isEmpty()) {
            return Collections.emptyList();
        }
        C chosen = RandomUtils.chooseRandom(options);
        if (chosen == null) {
            return Collections.emptyList();
        }
        customizeOption(chosen, template, effectInfo, text);
        TextRenderRequest<C> req = new TextRenderRequest<>();
        req.setDraftId(draftId);
        req.setText(text);
        req.setStart(start);
        req.setEnd(end);
        req.setCanvasWidth(canvasWidth);
        req.setCanvasHeight(canvasHeight);
        req.setStrategyOptions(chosen);
        return build(req);
    }
}


