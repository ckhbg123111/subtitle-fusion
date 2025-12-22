package com.zhongjia.subtitlefusion.model;

import lombok.Data;

import java.util.List;

@Data
public class WebtoonDramaSegmentInfo {
    /**
     * 关键帧图片
     */
    private String pictureUrl;
    /**
     * 音频及其字幕信息
     */
    private List<AudioInfo> audioInfo;
    /**
     * 段时长（即段内音频时长之和，关键帧持续时长）
     */
    private Long duration;

    /**
     * 关键帧定义
     */
    // 待补充的关键帧定义，应用于当前图片


    @Data
    public static class AudioInfo {
        /**
         * 音频
         */
        private String audioUrl;

        /**
         * 音频时长
         */
        private Long audioDuration;
        /**
         * 字幕
         */
        private List<CommonSubtitleInfo> commonSubtitleInfoList;
    }
}
