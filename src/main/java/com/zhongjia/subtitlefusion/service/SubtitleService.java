package com.zhongjia.subtitlefusion.service;

import com.zhongjia.subtitlefusion.model.SubtitleInfo;
import com.zhongjia.subtitlefusion.model.SubtitleTemplate;

import com.zhongjia.subtitlefusion.service.api.CapCutApiClient;
import com.zhongjia.subtitlefusion.service.subtitle.TextRenderStrategy;
import com.zhongjia.subtitlefusion.util.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubtitleService {

    private final CapCutApiClient apiClient;
    private final List<TextRenderStrategy<?>> strategies;

    private boolean checkValid(SubtitleInfo subtitleInfo){
        return subtitleInfo != null && subtitleInfo.getCommonSubtitleInfoList() != null && subtitleInfo.getSubtitleTemplate() != null;
    }

    public void processSubtitles(String draftId,
                                 SubtitleInfo subtitleInfo,
                                 int canvasWidth,
                                 int canvasHeight) throws Exception {
        if (!checkValid(subtitleInfo)) {
            throw new Exception("参数不合法,缺失字幕或字幕模板");
        }
        SubtitleTemplate subtitleTemplate = subtitleInfo.getSubtitleTemplate();

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
            TextRenderStrategy<?> strategy = selectStrategy(si.getSubtitleEffectInfo());
            List<Map<String, Object>> payloads = strategy.buildWithAutoOptions(
                    draftId, text, start, end, canvasWidth, canvasHeight, subtitleTemplate, si.getSubtitleEffectInfo()
            );
            if (CollectionUtils.isEmpty(payloads)) {
                log.warn("[SubtitleService] no payloads generated for strategy {} (maybe options empty)", strategy.supports());
                continue;
            }
            dispatchPayloads(payloads);
        }
    }

    private TextRenderStrategy<?> selectStrategy(SubtitleInfo.SubtitleEffectInfo subtitleEffectInfo) {
        if (subtitleEffectInfo != null) {
            for (TextRenderStrategy<?> s : strategies) {
                if (s.supports().equals(subtitleEffectInfo.getTextStrategy())) return s;
            }
        }
        return strategies.get(strategies.size() - 1); // 兜底
    }

    private void dispatchPayloads(List<Map<String, Object>> payloads) {
        for (Map<String, Object> p : payloads) {
            if (p.containsKey("template_id")) {
                apiClient.addTextTemplate(p);
            } else {
                apiClient.addText(p);
            }
        }
    }
}


