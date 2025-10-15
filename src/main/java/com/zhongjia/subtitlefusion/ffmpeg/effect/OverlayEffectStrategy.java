package com.zhongjia.subtitlefusion.ffmpeg.effect;

import com.zhongjia.subtitlefusion.model.VideoChainRequest;

import java.util.List;

/**
 * 统一的叠加动效策略接口（图片与 SVG 通用）。
 */
public interface OverlayEffectStrategy {

	/**
	 * 构建当前叠加元素的滤镜子链，并返回新的 last 标签。
	 */
	String apply(List<String> chains,
	            String last,
	            int inIndex,
	            String startSec,
	            String endSec,
	            String baseX,
	            String baseY,
	            OverlayEffectSupport support,
	            VideoChainRequest.OverlayElement element);
}


