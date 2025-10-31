package com.zhongjia.subtitlefusion.ffmpeg.effect.textbox;

import com.zhongjia.subtitlefusion.model.enums.OverlayEffectType;

public interface TextBoxEffectStrategyResolver {
    TextBoxEffectStrategy resolve(OverlayEffectType type);
}


