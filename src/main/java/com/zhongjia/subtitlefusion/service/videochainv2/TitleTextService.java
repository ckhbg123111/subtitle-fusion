package com.zhongjia.subtitlefusion.service.videochainv2;

import com.zhongjia.subtitlefusion.model.SubtitleInfo;
import com.zhongjia.subtitlefusion.service.SubtitleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 标题字幕服务：复用通用 SubtitleService 的策略与车道规划，但使用专用轨道名（如 title_fx）。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TitleTextService {

    private final SubtitleService subtitleService;

    public void processTitles(String draftId, SubtitleInfo titleInfo, int canvasWidth, int canvasHeight) throws Exception {
        // 使用专用基础轨道名，避免与底部字幕混用轨道
        subtitleService.processSubtitlesOnTrack(draftId, titleInfo, canvasWidth, canvasHeight, "title_fx");
    }
}


