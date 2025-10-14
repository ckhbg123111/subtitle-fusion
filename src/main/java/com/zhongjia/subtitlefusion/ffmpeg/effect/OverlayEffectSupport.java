package com.zhongjia.subtitlefusion.ffmpeg.effect;

/**
 * 提供 FilterChainBuilder 内部的工具方法委托，避免策略直接依赖实现类。
 */
public interface OverlayEffectSupport {
	String tag();
}


