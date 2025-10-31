package com.zhongjia.subtitlefusion.ffmpeg.effect;

import com.zhongjia.subtitlefusion.model.VideoChainRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * 淡入 + 停留漂浮 + 淡出 动效（图片与 SVG 通用）。
 */
@Component
public class FadeInFadeOutEffectStrategy implements OverlayEffectStrategy {
    @Override
    public String apply(List<String> chains, String last, int inIndex, String startSec, String endSec, String baseX, String baseY, OverlayEffectSupport support, VideoChainRequest.OverlayElement element) {
        String pLoop = support.tag();
        // 固定图片为 200x200；SVG 保持原尺寸
        if (element instanceof VideoChainRequest.PictureInfo) {
            chains.add("[" + inIndex + ":v]scale=200:200,format=rgba,loop=loop=-1:size=1:start=0,setpts=N/FRAME_RATE/TB" + pLoop);
        } else {
            chains.add("[" + inIndex + ":v]format=rgba,loop=loop=-1:size=1:start=0,setpts=N/FRAME_RATE/TB" + pLoop);
        }

        // 计算裁剪到入出场区间
        String duration = calcDur(startSec, endSec);
        String ptrim = support.tag();
        chains.add(pLoop + "trim=duration=" + duration + ptrim);

        String pshift = support.tag();
        chains.add(ptrim + "setpts=PTS+" + startSec + "/TB" + pshift);

        // 时长参数（使用较为自然的时长）
        String inDur = "0.15";   // 0.2s 淡入
        String outDur = "0.15";  // 0.2s 淡出

        // 在输入流上做淡入淡出（alpha=1 代表对 alpha 通道执行）
        String pfadeIn = support.tag();
        chains.add(pshift + "fade=t=in:st=" + startSec + ":d=" + inDur + ":alpha=1" + pfadeIn);

        String fadeOutStart = subtractSeconds(endSec, outDur);
        String pfadeOut = support.tag();
        chains.add(pfadeIn + "fade=t=out:st=" + fadeOutStart + ":d=" + outDur + ":alpha=1" + pfadeOut);

        // 位置：图片保持静止；SVG可保留原有轻微漂浮
        String xExpr;
        String yExpr;
        if (element instanceof VideoChainRequest.PictureInfo) {
            xExpr = baseX;
            yExpr = baseY;
        } else {
            xExpr = baseX + "+(W*0.0035)*sin(2*PI*(t*0.35))";
            yExpr = baseY + "+(H*0.0035)*sin(2*PI*(t*0.40))";
        }

        String out = support.tag();
        chains.add(last + pfadeOut + "overlay=x='" + xExpr.replace("'", "\\'") + "':y='" + yExpr.replace("'", "\\'") + "':enable='between(t," + startSec + "," + endSec + ")'" + out);
        return out;
    }

    private String calcDur(String startSec, String endSec) {
        try {
            double s = Double.parseDouble(startSec);
            double e = Double.parseDouble(endSec);
            double d = Math.max(0d, e - s);
            return String.format(Locale.US, "%.3f", d);
        } catch (Exception ignore) {
            return "0";
        }
    }

    private String subtractSeconds(String base, String delta) {
        try {
            double b = Double.parseDouble(base);
            double d = Double.parseDouble(delta);
            double v = Math.max(0d, b - d);
            return String.format(Locale.US, "%.3f", v);
        } catch (Exception ignore) {
            return "0";
        }
    }
}


