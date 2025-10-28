package com.zhongjia.subtitlefusion.ffmpeg.ass;

import com.zhongjia.subtitlefusion.model.SubtitleFusionV2Request;

/**
 * 左侧滑入 + 末端轻微回弹（通过位置偏移和缩放微调近似）。
 */
public class LeftInBounceStrategy implements AssEffectStrategy {
    @Override
    public String buildOverrideTags(SubtitleFusionV2Request.CommonSubtitleInfo lineInfo) {
        // 仅做淡入+稍加描边，移除缩放避免放大
        return "\\fad(120,140)\\bord2";
    }
}


