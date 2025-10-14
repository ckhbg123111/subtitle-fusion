package com.zhongjia.subtitlefusion.ffmpeg.effect;

import com.zhongjia.subtitlefusion.model.VideoChainRequest;

import java.util.List;

/**
 * 贴图动效策略接口。
 */
public interface OverlayEffectStrategy {

	/**
	 * 构建当前图片贴图的滤镜子链，并返回新的 last 标签。
	 *
	 * @param chains  整体滤镜链列表（会在本方法内追加片段）
	 * @param last    上一个视频流标签（如 "[0:v]" 或中间标签）
	 * @param inIndex 当前贴图素材在命令输入中的索引（如 2、3 ...）
	 * @param startSec 开始时间（秒，字符串）
	 * @param endSec   结束时间（秒，字符串）
	 * @param baseX   目标基准 X 表达式
	 * @param baseY   目标基准 Y 表达式
	 * @param support 提供 tag/escape 等工具方法
	 * @param pi      图片信息
	 * @return 新的 last 标签
	 */
	String apply(List<String> chains,
	            String last,
	            int inIndex,
	            String startSec,
	            String endSec,
	            String baseX,
	            String baseY,
	            OverlayEffectSupport support,
	            VideoChainRequest.PictureInfo pi);
}


