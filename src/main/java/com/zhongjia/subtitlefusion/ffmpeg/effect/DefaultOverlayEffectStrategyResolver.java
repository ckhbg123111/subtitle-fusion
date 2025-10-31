package com.zhongjia.subtitlefusion.ffmpeg.effect;

import com.zhongjia.subtitlefusion.model.VideoChainRequest;
import com.zhongjia.subtitlefusion.model.enums.OverlayEffectType;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 默认的图片叠加动效策略解析器。
 */
@Component
public class DefaultOverlayEffectStrategyResolver implements OverlayEffectStrategyResolver {
    @Autowired
    private LeftInRightOutEffectStrategy leftInRightOutEffectStrategy;
    @Autowired
    private LeftInBlindsOutEffectStrategy leftInBlindsOutEffectStrategy;
    @Autowired
    private BlindsInClockOutEffectStrategy blindsInClockOutEffectStrategy;
    @Autowired
    private FadeInFadeOutEffectStrategy fadeInFadeOutEffectStrategy;
    @Override
    public OverlayEffectStrategy resolve(VideoChainRequest.PictureInfo pi) {
        OverlayEffectType type = pi != null ? pi.getEffectType() : null;
        if (type == null) type = OverlayEffectType.LEFT_IN_BLINDS_OUT;
        switch (type) {
            case LEFT_IN_RIGHT_OUT:
                return leftInRightOutEffectStrategy;
            case FLOAT_WAVE:
                // 去除漂浮：将 FLOAT_WAVE 映射为淡入淡出（静止）
                return fadeInFadeOutEffectStrategy;
            case LEFT_IN_BLINDS_OUT:
                return leftInBlindsOutEffectStrategy;
            default:
                return blindsInClockOutEffectStrategy;
                
        }
    }
}


