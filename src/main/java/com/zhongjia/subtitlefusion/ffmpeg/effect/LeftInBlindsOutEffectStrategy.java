package com.zhongjia.subtitlefusion.ffmpeg.effect;

import com.zhongjia.subtitlefusion.model.VideoChainRequest;

import java.util.List;

/**
 * 左进 + 百叶窗出场：
 * - 入场：从左侧画外滑入到基准点
 * - 停留：基准点轻微漂浮
 * - 出场：按垂直百叶窗条带依次透明消失（停留在原位，不再滑动）
 */
public class LeftInBlindsOutEffectStrategy implements OverlayEffectStrategy {
	@Override
	public String apply(List<String> chains, String last, int inIndex, String startSec, String endSec, String baseX, String baseY, OverlayEffectSupport support, VideoChainRequest.OverlayElement element) {
		String pLoop = support.tag();
		chains.add("[" + inIndex + ":v]format=rgba,loop=loop=-1:size=1:start=0,setpts=N/FRAME_RATE/TB" + pLoop);

		String inDur = "0.40";   // 入场 0.4s
		String outDur = "0.60";  // 出场（百叶窗）0.6s

		String ptrim = support.tag();
		chains.add(pLoop + "trim=duration=" + calcDur(startSec, endSec) + ptrim);

		String pshift = support.tag();
		chains.add(ptrim + "setpts=PTS+" + startSec + "/TB" + pshift);

		// 停留阶段轻微漂浮
		String stayX = baseX + "+(W*0.0040)*sin(2*PI*(t*0.35))";
		String stayY = baseY + "+(H*0.0040)*sin(2*PI*(t*0.40))";

		// x 轴：左侧滑入后保持漂浮，不再右滑
		String xExpr = "if(lt(t," + startSec + "+" + inDur + "),(-w)+((t-" + startSec + ")/" + inDur + ")*(" + baseX + "+w)," + stayX + ")";
		String yExpr = stayY;

		// 出场：百叶窗消失（通过 geq 动态修改 alpha 平面）。
		// 思路：将画面水平按 N 条竖向条带划分，按时间进度 p 依次将左侧条带 alpha 置 0。
		int stripes = 12; // 条带数
		String progress = "clip((T-(" + endSec + "-" + outDur + "))/" + outDur + ",0,1)";
		String aExpr = "if(lt(T," + endSec + "-" + outDur + "),alpha(X,Y), if(lt(floor(X/(W/" + stripes + ")), floor((" + progress + ")*" + stripes + ")), 0, alpha(X,Y)))";

		String pgeq = support.tag();
		String geq = "geq=r='r(X,Y)':g='g(X,Y)':b='b(X,Y)':a='" + aExpr.replace("'", "\\'") + "'";
		chains.add(pshift + geq + pgeq);

		String out = support.tag();
		chains.add(last + pgeq + "overlay=x='" + xExpr.replace("'", "\\'") + "':y='" + yExpr.replace("'", "\\'") + "':enable='between(t," + startSec + "," + endSec + ")'" + out);
		return out;
	}

	private String calcDur(String startSec, String endSec) {
		try {
			double s = Double.parseDouble(startSec);
			double e = Double.parseDouble(endSec);
			double d = Math.max(0d, e - s);
			return String.format(java.util.Locale.US, "%.3f", d);
		} catch (Exception ignore) {
			return "0";
		}
	}
}



