package com.zhongjia.subtitlefusion.model;

import com.zhongjia.subtitlefusion.model.enums.AssSubtitleEffectTypeEnum;
import com.zhongjia.subtitlefusion.model.enums.OverlayEffectType;
import lombok.Data;

import java.util.List;

@Data
public class VideoChainRequest {
    private String taskId;
    private List<SegmentInfo> segmentList;
    
    private BgmInfo bgmInfo;
    /**
     * 逐段间转场配置：
     * - 为 null 表示不做任何段间转场，直接无损拼接；
     * - 非 null 时，长度应为 segmentList.size() - 1；每个项指定相邻两段之间的动效与时长；
     * - 本次改造不提供全局默认；若缺项或字段缺失应由上层在入参校验阶段拦截。
     */
    private List<GapTransitionSpec> gapTransitions;

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

    // 用于替换private String srtUrl; 烧录将采用ass方案，项目中已经有相关逻辑，可复用
    @Data
    public static class CommonSubtitleInfo {
        private String text;
        private String startTime;
        private String endTime;
        private AssSubtitleEffectTypeEnum subtitleEffectType;
    }

    // todo
    //  以前的文字+文本框效果是通过svg方案实现的，现在改为图片叠加文字的方式。
    //  图片参数定义了宽高，文本框也定义了宽高，二者中心在同一位置
    //  文本支持动态自动换行,随着文本量增大，字体需相应减小以容纳更多文本
    @Data
    public static class TextBoxInfo {
        private String text;
        private String startTime;
        private String endTime;
        private BoxInfo boxInfo;
        private Position position;
    }

    @Data
    public static class BoxInfo {
        private String boxPictureUrl;
        private Integer boxWidth;
        private Integer boxHeight;
        private Integer textWidth;
        private Integer textHeight;
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

    @Data
    public static class GapTransitionSpec {
        /** 与 FFmpeg xfade 的 transition 名称对应的类型（必填） */
        private TransitionType type;
        /** 该段间转场时长（秒，必填且 > 0） */
        private Double durationSec;
    }
}
