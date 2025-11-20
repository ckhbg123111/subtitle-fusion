package com.zhongjia.subtitlefusion.service;

import com.zhongjia.subtitlefusion.model.SubtitleInfo;
import com.zhongjia.subtitlefusion.model.SubtitleTemplate;
import com.zhongjia.subtitlefusion.model.options.*;
import com.zhongjia.subtitlefusion.service.api.CapCutApiClient;
import com.zhongjia.subtitlefusion.service.subtitle.FlowerTextStrategy;
import com.zhongjia.subtitlefusion.service.subtitle.KeywordHighlightStrategy;
import com.zhongjia.subtitlefusion.service.subtitle.TextRenderStrategy;
import com.zhongjia.subtitlefusion.service.subtitle.TextTemplateStrategy;
import com.zhongjia.subtitlefusion.util.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubtitleService {

    private final CapCutApiClient apiClient;
    private final List<TextRenderStrategy<?>> strategies;

    public void processSubtitles(String draftId,
                                 SubtitleInfo subtitleInfo,
                                 SubtitleTemplate subtitleTemplate,
                                 int canvasWidth,
                                 int canvasHeight) {
        if (subtitleInfo == null || subtitleInfo.getCommonSubtitleInfoList() == null) return;
        List<SubtitleInfo.CommonSubtitleInfo> items = subtitleInfo.getCommonSubtitleInfoList();
        log.info("[SubtitleService] subtitles: {}", items.size());

        // 按开始时间排序，保证渲染顺序稳定
        items.sort(Comparator.comparingDouble(si -> TimeUtils.parseToSeconds(si != null ? si.getStartTime() : null)));

        for (SubtitleInfo.CommonSubtitleInfo si : items) {
            if (si == null || si.getText() == null || si.getText().isEmpty()) continue;
            double start = TimeUtils.parseToSeconds(si.getStartTime());
            double end = TimeUtils.parseToSeconds(si.getEndTime());
            if (end <= start) end = start + 1.0;
            String text = si.getText();
            TextRenderStrategy<?> strategy = selectStrategy(si);
            // 构建请求并按策略类型填充专属参数
            List<Map<String, Object>> payloads;
            if (strategy instanceof KeywordHighlightStrategy) {
                TextRenderRequest<KeywordHighlightOptions> req = new TextRenderRequest<>();
                req.setDraftId(draftId);
                req.setText(text);
                req.setStart(start);
                req.setEnd(end);
                req.setCanvasWidth(canvasWidth);
                req.setCanvasHeight(canvasHeight);
                List<KeywordHighlightOptions> keywordHighlightOptions = subtitleTemplate.getKeywordHighlightOptions();
                if(CollectionUtils.isEmpty(keywordHighlightOptions)){
                    log.warn("[SubtitleService] keywordHighlightOptions is empty");
                    continue;
                }
                Collections.shuffle(keywordHighlightOptions);
                KeywordHighlightOptions opt = keywordHighlightOptions.get(0);
                if (si.getSubtitleEffectInfo() != null) {
                    opt.setKeywords(si.getSubtitleEffectInfo().getKeyWords());
                }
                req.setStrategyOptions(opt);
                payloads = ((KeywordHighlightStrategy) strategy).build(req);
            } else if (strategy instanceof FlowerTextStrategy) {
                TextRenderRequest<FlowerTextOptions> req = new TextRenderRequest<>();
                req.setDraftId(draftId);
                req.setText(text);
                req.setStart(start);
                req.setEnd(end);
                req.setCanvasWidth(canvasWidth);
                req.setCanvasHeight(canvasHeight);
                List<FlowerTextOptions> flowerTextOptions = subtitleTemplate.getFlowerTextOptions();
                if(CollectionUtils.isEmpty(flowerTextOptions)){
                    log.warn("[SubtitleService] flowerTextOptions is empty");
                    continue;
                }
                Collections.shuffle(flowerTextOptions);
                FlowerTextOptions opt = flowerTextOptions.get(0);
                req.setStrategyOptions(opt);
                payloads = ((FlowerTextStrategy) strategy).build(req);
            } else if (strategy instanceof TextTemplateStrategy) {
                TextRenderRequest<TextTemplateOptions> req = new TextRenderRequest<>();
                req.setDraftId(draftId);
                req.setText(text);
                req.setStart(start);
                req.setEnd(end);
                req.setCanvasWidth(canvasWidth);
                req.setCanvasHeight(canvasHeight);
                List<TextTemplateOptions> textTemplateOptions = subtitleTemplate.getTextTemplateOptions();
                if(CollectionUtils.isEmpty(textTemplateOptions)){
                    log.warn("[SubtitleService] textTemplateOptions is empty");
                    continue;
                }
                Collections.shuffle(textTemplateOptions);
                TextTemplateOptions opt = textTemplateOptions.get(0);
                req.setStrategyOptions(opt);
                payloads = ((TextTemplateStrategy) strategy).build(req);
            } else {
                // 基础策略或其它采用无专属参数
                TextRenderRequest<BasicTextOptions> req = new TextRenderRequest<>();
                req.setDraftId(draftId);
                req.setText(text);
                req.setStart(start);
                req.setEnd(end);
                req.setCanvasWidth(canvasWidth);
                req.setCanvasHeight(canvasHeight);
                // 为基础策略准备一份仅含通用字段的 options
                List<BasicTextOptions> basicTextOptions = subtitleTemplate.getBasicTextOptions();
                if(CollectionUtils.isEmpty(basicTextOptions)){
                    log.warn("[SubtitleService] basicTextOptions is empty");
                    continue;
                }
                Collections.shuffle(basicTextOptions);
                BasicTextOptions opt = basicTextOptions.get(0);
                req.setStrategyOptions(opt);
                @SuppressWarnings("unchecked")
                TextRenderStrategy<BasicTextOptions> s = (TextRenderStrategy<BasicTextOptions>) strategy;
                payloads = s.build(req);
            }

            for (Map<String, Object> p : payloads) {
                if (p.containsKey("template_id")) {
                    apiClient.addTextTemplate(p);
                } else {
                    apiClient.addText(p);
                }
            }
        }
    }

    private TextRenderStrategy<?> selectStrategy(SubtitleInfo.CommonSubtitleInfo si) {
        for (TextRenderStrategy<?> s : strategies) {
            if (s.supports(si)) return s;
        }
        return strategies.get(strategies.size() - 1); // 兜底
    }
}


