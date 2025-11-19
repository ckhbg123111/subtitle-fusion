package com.zhongjia.subtitlefusion.model.options;

import lombok.Data;

@Data
public class TextRenderCommonOptions {
    private String font;
    private String fontColor;
    private Integer fontSizeRate;
    private Integer borderWidthRate;
    private String borderColor;
    private Double transformX;
    private Double transformY;
    private CapCutTextAnimationEffectConfig textOutro;
    private CapCutTextAnimationEffectConfig textIntro;
}


