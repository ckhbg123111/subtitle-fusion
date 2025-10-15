package com.zhongjia.subtitlefusion.ffmpeg.effect;

import com.zhongjia.subtitlefusion.model.VideoChainRequest;

import java.util.List;

/**
 * 与现有实现等价的默认“浮动波动”动效策略（图片与 SVG 通用）。
 */
public class FloatWaveEffectStrategy implements OverlayEffectStrategy {
	@Override
	public String apply(List<String> chains, String last, int inIndex, String startSec, String endSec, String baseX, String baseY, OverlayEffectSupport support, VideoChainRequest.OverlayElement element) {
		String pLoop = support.tag();
		chains.add("[" + inIndex + ":v]format=rgba,loop=loop=-1:size=1:start=0,setpts=N/FRAME_RATE/TB" + pLoop);

		String c0 = support.tag();
		String c1 = support.tag();
		chains.add("color=c=black@0.0:s=64x64:r=60" + c0);
		chains.add("color=c=black@0.0:s=64x64:r=60" + c1);

		String cmx = support.tag();
		String p1 = support.tag();
		chains.add(c0 + pLoop + "scale2ref" + cmx + p1);

		String cmy = support.tag();
		String p2 = support.tag();
		chains.add(c1 + p1 + "scale2ref" + cmy + p2);

		String mx = support.tag();
		String my = support.tag();
		chains.add(cmx + "geq=lum='128+6*sin(2*PI*(Y/64)+T*2)'" + mx);
		chains.add(cmy + "geq=lum='128+4*sin(2*PI*(X/64)+T*2)'" + my);

		String pwave = support.tag();
		chains.add(p2 + mx + my + "displace=edge=smear" + pwave);

		String ptrim = support.tag();
		chains.add(pwave + "trim=duration=" + calcDur(startSec, endSec) + ptrim);

		String pshift = support.tag();
		chains.add(ptrim + "setpts=PTS+" + startSec + "/TB" + pshift);

		String xMove = baseX + "+(W*0.0075)*sin(2*PI*(t*0.35))";
		String yMove = baseY + "+(H*0.0075)*sin(2*PI*(t*0.40))";

		String out = support.tag();
		chains.add(last + pshift + "overlay=x=" + xMove + ":y=" + yMove + ":enable='between(t," + startSec + "," + endSec + ")'" + out);
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


