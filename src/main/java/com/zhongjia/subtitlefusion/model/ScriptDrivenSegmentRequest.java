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
	 * 旁白音频 URL
	 */
	@JsonProperty("audio_url")
	private String audioUrl;

	@JsonProperty("video_info")
	private List<VideoInfo> videoInfo;

	/**
	 * 画面中需要叠加/出现的物体或文字信息
	 */
	@JsonProperty("object_info")
	private List<ObjectItem> objectInfo;


	@Data
	public static class VideoInfo {
		@JsonProperty("video_url")
		private String videoUrl;
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

        /**
         * 角色对应位置 LEFT，RIGHT  如果角色在左边，图片（文本替换svg）就放在右边
         */
        @JsonProperty("role_position")
        private String rolePosition;
	}
}


