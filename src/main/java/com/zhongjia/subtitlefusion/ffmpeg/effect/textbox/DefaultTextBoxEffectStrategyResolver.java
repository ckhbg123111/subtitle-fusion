package com.zhongjia.subtitlefusion.ffmpeg.effect.textbox;

import com.zhongjia.subtitlefusion.model.enums.OverlayEffectType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DefaultTextBoxEffectStrategyResolver implements TextBoxEffectStrategyResolver {

    @Autowired
    private LeftInRightOutTextBoxEffectStrategy leftInRightOut;
    @Autowired
    private TopInFadeOutTextBoxEffectStrategy topInFadeOut;
    @Autowired
    private LeftInBlindsOutTextBoxEffectStrategy leftInBlindsOut;
    @Autowired
    private BlindsInClockOutTextBoxEffectStrategy blindsInClockOut;
    @Autowired
    private FadeInFadeOutTextBoxEffectStrategy fadeInFadeOut;

    @Override
    public TextBoxEffectStrategy resolve(OverlayEffectType type) {
        if (type == null) type = OverlayEffectType.FADE_IN_FADE_OUT;
        switch (type) {
            case LEFT_IN_RIGHT_OUT:
                return leftInRightOut;
            case TOP_IN_FADE_OUT:
                return topInFadeOut;
            case LEFT_IN_BLINDS_OUT:
                return leftInBlindsOut;
            case BLINDS_IN_CLOCK_OUT:
                return blindsInClockOut;
            case FADE_IN_FADE_OUT:
                return fadeInFadeOut;
            case FLOAT_WAVE:
            default:
                return fadeInFadeOut;
        }
    }
}


