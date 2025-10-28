package com.zhongjia.subtitlefusion.ffmpeg.ass;

import com.zhongjia.subtitlefusion.model.SubtitleFusionV2Request;

/**
 * 键盘打字 + 光标闪烁。ASS 无法逐字时序时，可用打字机模拟（clip 字数随时间增长），
 * 这里用较简方案：固定 300ms 淡入。
 */
public class TypewriterCursorStrategy implements AssEffectStrategy {
    @Override
    public String buildOverrideTags(SubtitleFusionV2Request.CommonSubtitleInfo lineInfo, int playX, int playY) {
        // 仅淡入，避免缩放放大抵消字号调整
        return "\\fad(150,120)";
    }
}


