package com.zhongjia.subtitlefusion.service.subtitle;

import com.zhongjia.subtitlefusion.model.SubtitleInfo;

import java.util.List;
import java.util.Map;

public interface TextRenderStrategy {
    boolean supports(SubtitleInfo.CommonSubtitleInfo si);

    List<Map<String, Object>> build(String draftId,
                                    SubtitleInfo.CommonSubtitleInfo si,
                                    double start,
                                    double end,
                                    String textIntro,
                                    String textOutro,
                                    int canvasWidth,
                                    int canvasHeight);
}


