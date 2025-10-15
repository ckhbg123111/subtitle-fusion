package com.zhongjia.subtitlefusion.ffmpeg.effect;

import com.zhongjia.subtitlefusion.model.VideoChainRequest;

/**
 * 策略解析器：根据叠加元素信息解析对应的动效策略实现。
 */
public interface OverlayEffectStrategyResolver {
    OverlayEffectStrategy resolve(VideoChainRequest.PictureInfo pi);
}


