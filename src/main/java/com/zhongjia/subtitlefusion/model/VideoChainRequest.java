package com.zhongjia.subtitlefusion.model;

import lombok.Data;

import java.util.List;

@Data
public class VideoChainRequest {
    private String taskId;
    private List<SegmentInfo> segmentList;

    @Data
    public static class SegmentInfo {
        private List<VideoInfo> videoInfos;
        private String audioUrl;
        // 便于 JSON 传参的字幕URL（SRT）
        private String srtUrl;
        private List<PictureInfo> pictureInfos;
        private List<KeywordsInfo> keywordsInfos;
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
    public static class PictureInfo {
        private String pictureUrl;
        private String startTime;
        private String endTime;
        private Position position;
		/**
		 * 贴图动效类型，默认 FLOAT_WAVE
		 */
		private EffectType effectType;
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
	 * 贴图动效枚举。可扩展更多类型。
	 */
	public enum EffectType {
		FLOAT_WAVE,
		LEFT_IN_RIGHT_OUT
	}
}
