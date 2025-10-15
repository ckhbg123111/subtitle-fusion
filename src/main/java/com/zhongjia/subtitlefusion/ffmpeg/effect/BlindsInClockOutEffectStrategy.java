package com.zhongjia.subtitlefusion.ffmpeg.effect;

import com.zhongjia.subtitlefusion.model.VideoChainRequest;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 百叶窗入场 + 时钟扫盘退场 动效（图片固定为 200x200，SVG 保持原尺寸）。
 * 思路：
 * - 入场：通过 geq 修改 alpha 平面，按竖向条带（百叶窗）从左到右依次显现。
 * - 退场：通过 geq 修改 alpha 平面，构造极坐标角度并以扇形（时钟盘）从 0..2π 扫掠为 0。
 */
@Component
public class BlindsInClockOutEffectStrategy implements OverlayEffectStrategy {
	@Override
	public String apply(List<String> chains, String last, int inIndex, String startSec, String endSec, String baseX, String baseY, OverlayEffectSupport support, VideoChainRequest.OverlayElement element) {
		String pLoop = support.tag();
		// 固定图片为 200x200（仅 Picture）；SVG 保持原尺寸
		if (element instanceof VideoChainRequest.PictureInfo) {
			chains.add("[" + inIndex + ":v]scale=200:200,format=rgba,loop=loop=-1:size=1:start=0,setpts=N/FRAME_RATE/TB" + pLoop);
		} else {
			chains.add("[" + inIndex + ":v]format=rgba,loop=loop=-1:size=1:start=0,setpts=N/FRAME_RATE/TB" + pLoop);
		}

		String inDur = "0.40";   // 入场 0.5s
		String outDur = "0.50";  // 退场 0.7s

		// 总时长裁剪并移位到 startSec
		String ptrim = support.tag();
		chains.add(pLoop + "trim=duration=" + calcDur(startSec, endSec) + ptrim);
		String pshift = support.tag();
		chains.add(ptrim + "setpts=PTS+" + startSec + "/TB" + pshift);

		// 轻微漂浮（停留阶段）
		String stayX = baseX + "+(W*0.0040)*sin(2*PI*(t*0.35))";
		String stayY = baseY + "+(H*0.0040)*sin(2*PI*(t*0.40))";

		// 入场：百叶窗展开（竖向条带自左向右显现）
		int stripes = 12;
		String inProgress = "clip((T-(" + startSec + "))/" + inDur + ",0,1)";
		String aInExpr = "if(lt(T," + startSec + "),0, if(lt(T," + startSec + "+" + inDur + "), if(lt(floor(X/(W/" + stripes + ")), floor((" + inProgress + ")*" + stripes + ")), alpha(X,Y), 0), alpha(X,Y)))";

		String pIn = support.tag();
		String geqIn = "geq=r='r(X,Y)':g='g(X,Y)':b='b(X,Y)':a='" + aInExpr.replace("'", "\\'") + "'";
		chains.add(pshift + geqIn + pIn);

		// 出场：时钟扫盘关闭（以中心点为极角，按时间扩大扇形范围置 0）
		String outStart = endSec + "-" + outDur;
		// 将坐标移动到素材中心，计算 atan2 角度 [ -pi, pi ]，映射到 [0, 2*pi)
		String angle = "(atan2((Y-h/2),(X-w/2))+PI)"; // 映射到 [0,2PI)
		String progress = "clip((T-(" + outStart + "))/" + outDur + ",0,1)";
		String aOutExpr = "if(lt(T," + outStart + "),alpha(X,Y), if(lt(" + angle + ", 2*PI*(" + progress + ")), 0, alpha(X,Y)))";

		String pOut = support.tag();
		String geqOut = "geq=r='r(X,Y)':g='g(X,Y)':b='b(X,Y)':a='" + aOutExpr.replace("'", "\\'") + "'";
		chains.add(pIn + geqOut + pOut);

		// 位置表达式：入场阶段固定位置，之后轻微漂浮（overlay 使用 t）
		String xExpr = "if(lt(t," + startSec + ")," + baseX + ", if(lt(t," + startSec + "+" + inDur + ")," + baseX + "," + stayX + "))";
		String yExpr = "if(lt(t," + startSec + ")," + baseY + ", if(lt(t," + startSec + "+" + inDur + ")," + baseY + "," + stayY + "))";

		String out = support.tag();
		chains.add(last + pOut + "overlay=x='" + xExpr.replace("'", "\\'") + "':y='" + yExpr.replace("'", "\\'") + "':enable='between(t," + startSec + "," + endSec + ")'" + out);
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


