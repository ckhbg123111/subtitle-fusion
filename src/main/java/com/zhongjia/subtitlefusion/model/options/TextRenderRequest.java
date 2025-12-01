package com.zhongjia.subtitlefusion.model.options;

import lombok.Data;

@Data
public class TextRenderRequest<C extends StrategyOptions> {
    private String draftId;
    private String text;
    private double start;
    private double end;
    private int canvasWidth;
    private int canvasHeight;
    private C strategyOptions;
}


