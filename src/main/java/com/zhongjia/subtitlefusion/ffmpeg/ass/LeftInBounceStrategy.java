package com.zhongjia.subtitlefusion.ffmpeg.ass;

import com.zhongjia.subtitlefusion.model.SubtitleFusionV2Request;

/**
 * 左侧滑入 + 末端轻微回弹（通过位置偏移和缩放微调近似）。
 */
public class LeftInBounceStrategy implements AssEffectStrategy {
    @Override
    public String buildOverrideTags(SubtitleFusionV2Request.CommonSubtitleInfo lineInfo, int playX, int playY) {
        // 参考给定示例，构造左入-回弹动效（按分辨率自适应位置）
        // 顶端居中 y 约 playY*0.24；从屏幕左外侧 -playX*0.36 滑入到中间 playX*0.28
        int y = Math.max(0, Math.round(playY * 0.24f));
        int xStart = -Math.round(playX * 0.36f);
        int xEnd = Math.round(playX * 0.28f);
        // 时间轴（毫秒）：0-380 入场；380-560 放大；560-720 缩小到略小；720-840 回到 100%；
        // 轻微旋转回正；较粗描边与微模糊收敛
        StringBuilder sb = new StringBuilder();
        sb.append("\\an8");
        sb.append("\\move(").append(xStart).append(",").append(y).append(",").append(xEnd).append(",").append(y).append(",0,380)");
        sb.append("\\bord6\\blur2\\t(0,380,\\blur0.3)\\b1");
        sb.append("\\t(380,560,\\fscx112\\fscy112\\fsp-6)");
        sb.append("\\t(560,720,\\fscx96\\fscy96\\fsp0)");
        sb.append("\\t(720,840,\\fscx100\\fscy100)");
        sb.append("\\t(380,620,\\frz2)\\t(620,780,\\frz0)");
        sb.append("\\1c&H0000FFFF&");
        return sb.toString();
    }
}


