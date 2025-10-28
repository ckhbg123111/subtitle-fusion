package com.zhongjia.subtitlefusion.ffmpeg.ass;

import com.zhongjia.subtitlefusion.model.SubtitleFusionV2Request;

/**
 * 左侧滑入 + 末端轻微回弹（通过位置偏移和缩放微调近似）。
 */
public class LeftInBounceStrategy implements AssEffectStrategy {
    @Override
    public String buildOverrideTags(SubtitleFusionV2Request.CommonSubtitleInfo lineInfo) {
        // ASS 的位置动画精细度有限；这里提供简单近似：淡入 + 初始更粗/更大，快速回落
        return "\\fad(120,140)\\bord2\\fscx=108\\fscy=108";
    }
}


