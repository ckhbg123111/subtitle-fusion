package com.zhongjia.subtitlefusion.ffmpeg.effect;

import com.zhongjia.subtitlefusion.model.VideoChainRequest;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 左进右出：
 * - 入场阶段：从左侧画外滑入到基准点
 * - 停留阶段：基准点轻微漂浮
 * - 出场阶段：向右滑出画外
 */
@Component
public class LeftInRightOutEffectStrategy implements OverlayEffectStrategy {
	@Override
	public String apply(List<String> chains, String last, int inIndex, String startSec, String endSec, String baseX, String baseY, OverlayEffectSupport support, VideoChainRequest.OverlayElement element) {
		String pLoop = support.tag();
		// 固定图片为 200x200；SVG 保持原尺寸
		if (element instanceof VideoChainRequest.PictureInfo) {
			chains.add("[" + inIndex + ":v]scale=200:200,format=rgba,loop=loop=-1:size=1:start=0,setpts=N/FRAME_RATE/TB" + pLoop);
		} else {
			chains.add("[" + inIndex + ":v]format=rgba,loop=loop=-1:size=1:start=0,setpts=N/FRAME_RATE/TB" + pLoop);
		}

		// 入场/停留/出场分段控制
		String inDur = "0.40";     // 入场 0.4s
		String outDur = "0.40";    // 出场 0.4s

		String ptrim = support.tag();
		chains.add(pLoop + "trim=start=0:duration=" + calcDur(startSec, endSec) + ptrim);

		String pshift = support.tag();
		chains.add(ptrim + "setpts=PTS+" + startSec + "/TB" + pshift);

		// 基准点轻微漂浮（停留阶段）
		String stayX = baseX + "+(W*0.0045)*sin(2*PI*(t*0.35))";
		String stayY = baseY + "+(H*0.0045)*sin(2*PI*(t*0.40))";

		// 拼接分段 x 表达式：
		// t < startSec+inDur:   从 -w 到 baseX 线性移动
		// t in [startSec+inDur, endSec-outDur]:  轻微漂浮 stayX
		// t > endSec-outDur: 从 baseX 到 画外 W 线性移动
		String xExpr = "if(lt(t," + startSec + "+" + inDur + "),(-w)+((t-" + startSec + ")/" + inDur + ")*(" + baseX + "+w)," +
				"if(lt(t," + endSec + "-" + outDur + ")," + stayX + ",(" + baseX + ")+((t-(" + endSec + "-" + outDur + "))/" + outDur + ")*(W-" + baseX + ") ))";

		String yExpr = stayY;

		String out = support.tag();
		chains.add(last + pshift + "overlay=x='" + xExpr.replace("'", "\\'") + "':y=" + yExpr + ":enable='between(t," + startSec + "," + endSec + ")'" + out);
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


