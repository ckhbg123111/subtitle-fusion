package com.zhongjia.subtitlefusion.model;

import lombok.Data;

import java.util.List;

@Data
public class VideoChainV2Request {
    /**
     * 任务ID
     */
    private String taskId;
    /**
     * 段落详情
     */
    private List<SegmentInfo> segmentList;

    /**
     * 整段的背景乐
     */
    private BgmInfo bgmInfo;
    /**
     * 逐段间转场配置：
     * - 为 null 表示不做任何段间转场
     * - 非 null 时，长度应为 segmentList.size() - 1；每个项指定相邻两段之间的动效与时长；
     * - 本次改造不提供全局默认；若缺项或字段缺失应由上层在入参校验阶段拦截。
     */
    private List<CapCutGapTransitionSpec> gapTransitions;

    @Data
    public static class SegmentInfo {
        // 段间小段无声视频
        private List<VideoInfo> videoInfos;
        // 音频地址
        private String audioUrl;
        // 底部字幕，不会出现时间重叠
        private SubtitleInfo subtitleInfo;
        // 标题字幕，可能出现时间轴重叠的情况
        private SubtitleInfo textInfo;
        // 图片
        private List<PictureInfo> pictureInfos;
        // 段时长,取决于音频时长，单位为毫秒
        private Integer duration;
    }

    @Data
    public static class VideoInfo {
        private String videoUrl;
    }


    @Data
    public static class PictureInfo {
        private String pictureUrl;
        private String startTime;
        private String endTime;
        private Position position;
        private String intro;
        private String outro;
        private String combo;
    }

    /**
     * 高度占比支持配置
     * 左右在画面中占比支持配置
     */
    public enum Position {
        LEFT,
        RIGHT
    }


    @Data
    public static class BgmInfo {
        private String backgroundMusicUrl;
        private Double bgmVolume; // 0.0 ~ 1.0，默认 0.25
        private Double bgmFadeInSec; // 可为 null 或 0 表示不淡入
        private Double bgmFadeOutSec; // 可为 null 或 0 表示不淡出
    }


    @Data
    public static class CapCutGapTransitionSpec {
        /**
         * 转场名称（必填）
         */
        private String transition;
        /**
         * 该段间转场时长（秒，可为 null  0.5  ）
         */
        private Double durationSec;
    }
}
