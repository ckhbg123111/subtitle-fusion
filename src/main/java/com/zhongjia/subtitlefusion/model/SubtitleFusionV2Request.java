package com.zhongjia.subtitlefusion.model;

import com.zhongjia.subtitlefusion.model.enums.OverlayEffectType;
import lombok.Data;

import java.util.List;

@Data
public class SubtitleFusionV2Request {
    private Integer taskId;
    private String videoUrl;
    private SubtitleInfo subtitleInfo;

    private static class SubtitleInfo {
        private List<CommonSubtitleInfo> commonSubtitleInfoList;
        private List<DrawTextSubtitleInfo> drawTextSubtitleInfoList;
        private List<PictureInfo> pictureInfoList;
    }

    @Data
    private static class CommonSubtitleInfo {
        private String text;
        private List<String> keyWords;
        private String startTime;
        private String endTime;
    }

    @Data
    private static class DrawTextSubtitleInfo {
        private String text;
        private String startTime;
        private String endTime;
        private OverlayEffectType effectType;
        private String effectAudioUrl;

    }

    @Data
    private static class PictureInfo {
        private String pictureUrl;
        private String startTime;
        private String endTime;
        private OverlayEffectType effectType;
        private String effectAudioUrl;
    }
}