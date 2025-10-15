package com.zhongjia.subtitlefusion.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 依据示例 JSON 结构的分段驱动请求模型（单条记录）。
 * 控制器可接收 List<ScriptDrivenSegmentRequest> 以适配根为数组的请求体。
 */
@Data
public class ScriptDrivenSegmentRequest {
	/**
	 * 整体脚本文本
	 */
	private String text;

	/**
	 * 旁白音频 URL
	 */
	@JsonProperty("audio_url")
	private String audioUrl;

	/**
	 * 视频片段与首尾帧等信息
	 */
	@JsonProperty("video_info")
	private List<VideoInfo> videoInfo;

	/**
	 * 字幕信息
	 */
	@JsonProperty("subtitle_info")
	private List<SubtitleItem> subtitleInfo;

	/**
	 * 画面中需要叠加/出现的物体或文字信息
	 */
	@JsonProperty("object_info")
	private List<ObjectItem> objectInfo;

	/**
	 * 音色标识
	 */
	@JsonProperty("audio_tone")
	private String audioTone;

	/**
	 * 音频时长（秒）
	 */
	@JsonProperty("audio_duration")
	private Double audioDuration;

	/**
	 * 画幅比例，如 16:9
	 */
	private String ratio;

	@Data
	public static class VideoInfo {
		@JsonProperty("video_url")
		private String videoUrl;

		@JsonProperty("first_frame")
		private FrameInfo firstFrame;

		@JsonProperty("last_frame")
		private FrameInfo lastFrame;

		@JsonProperty("original_video_url")
		private String originalVideoUrl;

		@JsonProperty("reversed_video_url")
		private String reversedVideoUrl;

		@JsonProperty("role_position")
		private String rolePosition; // 示例值：LEFT / RIGHT

		private String prompt;
	}

	@Data
	public static class FrameInfo {
		@JsonProperty("image_url")
		private String imageUrl;

		private String prompt;

		@JsonProperty("reference_image")
		private String referenceImage;
	}

	@Data
	public static class SubtitleItem {
		private String text;
		/**
		 * 起止时间，长度为 2 的数组：[start, end]，格式如 00:00:00,000
		 */
		private List<String> time;
	}

	@Data
	public static class ObjectItem {
		private String text;

		@JsonProperty("image_url")
		private String imageUrl; // 当 type 为 image 时可能存在

		/**
		 * 起止时间，长度为 2 的数组：[start, end]
		 */
		private List<String> time;

		/**
		 * 对象类型：text / image
		 */
		private String type;
	}
}


