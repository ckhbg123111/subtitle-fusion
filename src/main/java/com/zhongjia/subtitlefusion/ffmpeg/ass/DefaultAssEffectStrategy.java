package com.zhongjia.subtitlefusion.ffmpeg.ass;

import com.zhongjia.subtitlefusion.model.SubtitleFusionV2Request;

/**
 * 默认无特殊动效，仅给出轻微淡入淡出。
 */
public class DefaultAssEffectStrategy implements AssEffectStrategy {
    @Override
    public String buildOverrideTags(SubtitleFusionV2Request.CommonSubtitleInfo lineInfo) {
        // 200ms 淡入，150ms 淡出；适配中文视频普遍风格
        return "\\fad(200,150)";
    }
}


