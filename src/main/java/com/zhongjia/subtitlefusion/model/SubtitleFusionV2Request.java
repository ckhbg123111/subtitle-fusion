package com.zhongjia.subtitlefusion.model;

import com.zhongjia.subtitlefusion.model.enums.AssSubtitleEffectTypeEnum;
import com.zhongjia.subtitlefusion.model.enums.OverlayEffectType;
import lombok.Data;

import java.util.List;

@Data
public class SubtitleFusionV2Request {
    private Integer taskId;
    private String videoUrl;
    private SubtitleInfo subtitleInfo;

    @Data
    private static class SubtitleInfo {
        private List<CommonSubtitleInfo> commonSubtitleInfoList;
        private List<PictureInfo> pictureInfoList;
    }

    @Data
    private static class CommonSubtitleInfo {
        private String text;
        private String startTime;
        private String endTime;
        private SubtitleEffectInfo subtitleEffectInfo;
    }

    @Data
    private static class SubtitleEffectInfo {
        // Ass 字幕动效枚举
        private AssSubtitleEffectTypeEnum effectType;
        // 动效音效
        private String effectAudioUrl;
        // 适用于处理关键字的动效
        private List<String> keyWords;
    }

    @Data
    private static class PictureInfo {
        private String pictureUrl;
        private String startTime;
        private String endTime;
        // 插图动效
        private OverlayEffectType effectType;
        // 插图入场音效
        private String effectAudioUrl;
    }
}