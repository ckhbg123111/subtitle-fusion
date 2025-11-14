package com.zhongjia.subtitlefusion.model;

import com.zhongjia.subtitlefusion.model.enums.OverlayEffectType;
import lombok.Data;

import java.util.List;

@Data
public class VideoChainV2Request {
    private String taskId;
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
        private List<VideoInfo> videoInfos;
        private String audioUrl;
        // 便于 JSON 传参的字幕URL（SRT）
        private SubtitleInfo  subtitleInfo;
        private List<PictureInfo> pictureInfos;
        private List<KeywordsInfo> keywordsInfos;
        /** 新增：图片+文字文本框元素 */
        private List<TextBoxInfo> textBoxInfos;
    }

    @Data
    public static class TextBoxInfo implements OverlayElement {
        private String text;
        private String startTime;
        private String endTime;
        private BoxInfo boxInfo;
        private Position position;
        private TextStyle textStyle; // 可选样式，缺省走全局配置
        /**
         * 文本框动效类型（与图片、SVG 共用枚举）。缺省采用 FLOAT_WAVE。
         */
        private OverlayEffectType effectType;
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
    public static class TextStyle {
        /** 若为空，沿用 AppProperties.render.fontFile */
        private String fontFile;
        /** 颜色，默认 white，可支持 #RRGGBB */
        private String fontColor;
        /** 自适应字号下限 */
        private Integer fontSizeMin;
        /** 自适应字号上限 */
        private Integer fontSizeMax;
        /** 行距（像素） */
        private Integer lineSpacing;
        /** 文本框内部对齐方式：center|left|right（默认 center） */
        private String align;
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


    @Data
    public static class CapCutGapTransitionSpec {
        /** 与 FFmpeg xfade 的 transition 名称对应的类型（必填） */
        private String transition;
        /** 该段间转场时长（秒，必填且 > 0） */
        private Double durationSec;
    }
}
