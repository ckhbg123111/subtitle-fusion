package com.zhongjia.subtitlefusion.model.options;

import com.zhongjia.subtitlefusion.model.SubtitleInfo;
import lombok.Data;

@Data
public class TextRenderRequest<C extends StrategyOptions> {
    private String draftId;
    private SubtitleInfo.CommonSubtitleInfo subtitle;
    private double start;
    private double end;
    private int canvasWidth;
    private int canvasHeight;
    private C strategyOptions;
}


