package com.zhongjia.subtitlefusion.model;

import lombok.Data;

import java.util.List;

@Data
public class VideoChainRequest {
    private String taskId;
    private List<SegmentInfo> segmentList;
    
    private BgmInfo bgmInfo;
    /** 段间转场：为空表示关闭；非空时根据类型启用（时长硬编码） */
    private TransitionType transition;

    @Data
    public static class SegmentInfo {
        private List<VideoInfo> videoInfos;
        private String audioUrl;
        // 便于 JSON 传参的字幕URL（SRT）
        private String srtUrl;
        private List<PictureInfo> pictureInfos;
        private List<KeywordsInfo> keywordsInfos;
        private List<SvgInfo> svgInfos;
    }

    @Data
    public static class VideoInfo {
        private String videoUrl;
    }

    @Data
    public static class KeywordsInfo {
        private String keyword;
        private String startTime;
        private String endTime;
        private Position position;
    }

	@Data
	public static class PictureInfo implements OverlayElement {
        private String pictureUrl;
        private String imageBorderUrl;
        private String startTime;
        private String endTime;
        private Position position;
		/**
		 * 叠加动效类型（与 SVG 复用），默认 FLOAT_WAVE
		 */
		private OverlayEffectType effectType;
    }

	@Data
	public static class SvgInfo implements OverlayElement {
		private String svgBase64;
		private String startTime;
		private String endTime;
		private Position position;
		/**
		 * 叠加动效类型（与图片复用），默认 FADE_IN_FADE_OUT
		 */
		private OverlayEffectType effectType;
	}

    /**
     * 高度占比支持配置
     * 左右在画面中占比支持配置
     */
    public enum Position {
        LEFT,
        RIGHT
    }

	/**
	 * 统一的叠加动效类型（图片与 SVG 共用）。
	 */
	public enum OverlayEffectType {
		FLOAT_WAVE,
		LEFT_IN_RIGHT_OUT,
		TOP_IN_FADE_OUT,
		LEFT_IN_BLINDS_OUT,
		BLINDS_IN_CLOCK_OUT,
		FADE_IN_FADE_OUT
	}

	/**
	 * 统一叠加元素视图（供动效策略消费）。
	 */
	public interface OverlayElement {
		String getStartTime();
		String getEndTime();
		Position getPosition();
		OverlayEffectType getEffectType();
	}

	@Data
	public static class BgmInfo {
		private String backgroundMusicUrl;
		private Double bgmVolume; // 0.0 ~ 1.0，默认 0.25
		private Double bgmFadeInSec; // 可为 null 或 0 表示不淡入
		private Double bgmFadeOutSec; // 可为 null 或 0 表示不淡出
	}

    /**
     * 段间转场类型（与 FFmpeg xfade transition 名称对应，未来可扩展）。
     */
    public enum TransitionType {
        // xfade 支持列表（与 ffmpeg -h filter=xfade 输出一致的命名，Java 枚举用大写）
        CUSTOM,
        FADE,
        WIPELEFT,
        WIPERIGHT,
        WIPEUP,
        WIPEDOWN,
        SLIDELEFT,
        SLIDERIGHT,
        SLIDEUP,
        SLIDEDOWN,
        CIRCLECROP,
        RECTCROP,
        DISTANCE,
        FADEBLACK,
        FADEWHITE,
        RADIAL,
        SMOOTHLEFT,
        SMOOTHRIGHT,
        SMOOTHUP,
        SMOOTHDOWN,
        CIRCLEOPEN,
        CIRCLECLOSE,
        VERTOPEN,
        VERTCLOSE,
        HORZOPEN,
        HORZCLOSE,
        DISSOLVE,
        PIXELIZE,
        DIAGTL,
        DIAGTR,
        DIAGBL,
        DIAGBR,
        HLSLICE,
        HRSLICE,
        VUSLICE,
        VDSLICE,
        HBLUR,
        FADEGRAYS,
        WIPETL,
        WIPETR,
        WIPEBL,
        WIPEBR,
        SQUEEZEH,
        SQUEEZEV,
        ZOOMIN,
        FADEFAST,
        FADESLOW,
        HLWIND,
        HRWIND,
        VUWIND,
        VDWIND,
        COVERLEFT,
        COVERRIGHT,
        COVERUP,
        COVERDOWN,
        REVEALLEFT,
        REVEALRIGHT,
        REVEALUP,
        REVEALDOWN
    }
}
