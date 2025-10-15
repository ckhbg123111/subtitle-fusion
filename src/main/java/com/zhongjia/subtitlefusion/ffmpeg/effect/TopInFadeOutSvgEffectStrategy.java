package com.zhongjia.subtitlefusion.ffmpeg.effect;

import com.zhongjia.subtitlefusion.ffmpeg.FilterExprUtils;
import com.zhongjia.subtitlefusion.model.VideoChainRequest;

import java.util.List;
import java.util.Locale;

/**
 * 动效：从上到下入场（起始于 2 倍高度之上），并在结尾阶段淡出。（图片与 SVG 通用）
 */
public class TopInFadeOutSvgEffectStrategy implements OverlayEffectStrategy {
	@Override
	public String apply(List<String> chains, String last, int inIndex, String startSec, String endSec, String baseX, String baseY, OverlayEffectSupport support, VideoChainRequest.OverlayElement element) {
		String pLoop = support.tag();
		chains.add("[" + inIndex + ":v]format=rgba,loop=loop=-1:size=1:start=0,setpts=N/FRAME_RATE/TB" + pLoop);

		String ptrim = support.tag();
		chains.add(pLoop + "trim=duration=" + FilterExprUtils.calcDuration(startSec, endSec) + ptrim);

		String pshift = support.tag();
		chains.add(ptrim + "setpts=PTS+" + startSec + "/TB" + pshift);

		// 时间参数
		String inDur = "0.50";   // 入场时长 0.5s
		String outDur = "0.50";  // 淡出时长 0.5s

		// 对输入做末尾淡出（alpha=1），fade 的 st/d 仅接受常量值，需预先计算
		String pfade = support.tag();
		String fadeStart = subtractSeconds(endSec, outDur);
		chains.add(pshift + "fade=t=out:st=" + fadeStart + ":d=" + outDur + ":alpha=1" + pfade);

		// x 按基准点小幅漂浮
		String stayX = baseX + "+(W*0.0035)*sin(2*PI*(t*0.35))";
		// y: 从 -2h 开始下落到 baseY，之后轻微漂浮
		String yInExpr = "(-2*h)+((t-" + startSec + ")/" + inDur + ")*(" + baseY + "+2*h)"; // t=start 时 y=-2h, t=start+inDur 时 y=baseY
		String stayY = baseY + "+(H*0.0035)*sin(2*PI*(t*0.40))";
		String yExpr = "if(lt(t," + startSec + "+" + inDur + ")," + yInExpr + "," + stayY + ")";

		String out = support.tag();
		chains.add(last + pfade + "overlay=x='" + stayX.replace("'", "\\'") + "':y='" + yExpr.replace("'", "\\'") + "':enable='between(t," + startSec + "," + endSec + ")'" + out);
		return out;
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


