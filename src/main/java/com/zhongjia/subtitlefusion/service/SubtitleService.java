package com.zhongjia.subtitlefusion.service;

import com.zhongjia.subtitlefusion.model.SubtitleInfo;
import com.zhongjia.subtitlefusion.service.api.CapCutApiClient;
import com.zhongjia.subtitlefusion.service.subtitle.TextRenderStrategy;
import com.zhongjia.subtitlefusion.util.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubtitleService {

    private final CapCutApiClient apiClient;
    private final List<TextRenderStrategy> strategies;

		public void processSubtitles(String draftId,
									SubtitleInfo subtitleInfo,
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

            TextRenderStrategy strategy = selectStrategy(si);
				String textIntro = "羽化向右擦开";
				String textOutro = "渐隐";
//				String textOutro = apiClient.getRandomTextOutro();
            List<Map<String, Object>> payloads = strategy.build(draftId, si, start, end, textIntro, textOutro, canvasWidth, canvasHeight);
            for (Map<String, Object> p : payloads) {
                if (p.containsKey("template_id")) {
                    apiClient.addTextTemplate(p);
                } else {
                    apiClient.addText(p);
                }
            }
        }
    }

    private TextRenderStrategy selectStrategy(SubtitleInfo.CommonSubtitleInfo si) {
        for (TextRenderStrategy s : strategies) {
            if (s.supports(si)) return s;
        }
        return strategies.get(strategies.size() - 1); // 兜底
    }
}


