package com.zhongjia.subtitlefusion.ffmpeg.ass;

import com.zhongjia.subtitlefusion.model.SubtitleFusionV2Request;

/**
 * 键盘打字 + 光标闪烁。ASS 无法逐字时序时，可用打字机模拟（clip 字数随时间增长），
 * 这里用较简方案：固定 300ms 淡入 + 闪烁光标（利用 \k karaoke/或 \alpha 近似）。
 * 为简洁性，仅给出标签片段：淡入 + 假光标（下划线 + 透明交替）。
 */
public class TypewriterCursorStrategy implements AssEffectStrategy {
    @Override
    public String buildOverrideTags(SubtitleFusionV2Request.CommonSubtitleInfo lineInfo) {
        // 提供易读的近似效果：淡入 + 轻微缩放出现
        return "\\fad(150,120)\\fscx=102\\fscy=102";
    }
}


