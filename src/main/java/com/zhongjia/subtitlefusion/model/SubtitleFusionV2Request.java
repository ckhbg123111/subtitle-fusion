package com.zhongjia.subtitlefusion.model;

import com.zhongjia.subtitlefusion.model.enums.AssSubtitleEffectTypeEnum;
import com.zhongjia.subtitlefusion.model.enums.OverlayEffectType;
import lombok.Data;

import java.util.List;

@Data
public class SubtitleFusionV2Request {
    private String taskId;
    private String videoUrl;
    private SubtitleInfo subtitleInfo;

    @Data
    public static class SubtitleInfo {
        private List<CommonSubtitleInfo> commonSubtitleInfoList;
        private List<PictureInfo> pictureInfoList;
    }

    @Data
    public static class CommonSubtitleInfo {
        private String text;
        private String startTime;
        private String endTime;
        private SubtitleEffectInfo subtitleEffectInfo;
    }

    @Data
    public static class SubtitleEffectInfo {
        // Ass 字幕动效枚举
        @Deprecated
        private AssSubtitleEffectTypeEnum effectType;
        // 动效音效
        private String effectAudioUrl;
        // 适用于处理关键字的动效
        private List<String> keyWords;
        // 花字效果ID（对应 add_text 的 effect_effect_id）
        private String textEffectId;
        // 文字模板ID（对应 add_text_template 的 template_id）
        private String textTemplateId;
        // 文字模板内各占位文本（对应 add_text_template 的 texts）
        private List<String> templateTexts;
    }

    @Data
    public static class PictureInfo {
        private String pictureUrl;
        private String startTime;
        private String endTime;
        // 插图动效
        @Deprecated
        private OverlayEffectType effectType;
        // 插图入场音效
        private String effectAudioUrl;
    }
}