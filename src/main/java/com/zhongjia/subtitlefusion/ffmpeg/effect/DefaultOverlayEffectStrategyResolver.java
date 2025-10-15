package com.zhongjia.subtitlefusion.ffmpeg.effect;

import com.zhongjia.subtitlefusion.model.VideoChainRequest;
import org.springframework.stereotype.Component;

/**
 * 默认的图片叠加动效策略解析器。
 */
@Component
public class DefaultOverlayEffectStrategyResolver implements OverlayEffectStrategyResolver {
    @Override
    public OverlayEffectStrategy resolve(VideoChainRequest.PictureInfo pi) {
        VideoChainRequest.OverlayEffectType type = pi != null ? pi.getEffectType() : null;
        if (type == null) type = VideoChainRequest.OverlayEffectType.FLOAT_WAVE;
        switch (type) {
            case LEFT_IN_RIGHT_OUT:
                return new LeftInRightOutEffectStrategy();
            case LEFT_IN_BLINDS_OUT:
                return new LeftInBlindsOutEffectStrategy();
            case FLOAT_WAVE:
            default:
                return new FloatWaveEffectStrategy();
        }
    }
}


