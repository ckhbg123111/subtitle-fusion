package com.zhongjia.subtitlefusion.service.subtitle;

import com.zhongjia.subtitlefusion.model.SubtitleFusionV2Request;

import java.util.List;
import java.util.Map;

public interface TextRenderStrategy {
    boolean supports(SubtitleFusionV2Request.CommonSubtitleInfo si);

    List<Map<String, Object>> build(String draftId,
                                    SubtitleFusionV2Request.CommonSubtitleInfo si,
                                    double start,
                                    double end,
                                    String textIntro,
                                    String textOutro);
}


