package com.zhongjia.subtitlefusion.model;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class VideoChainRequest {
    private String taskId;
    private List<SegmentInfo> segmentList;

    @Data
    public static class SegmentInfo {
        private List<VideoInfo> videoInfos;
        private String audioUrl;
        // 同字幕烧录需求的字幕格式
        private MultipartFile srtFile;
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
    }

    /**
     * 高度占比支持配置
     * 左右在画面中占比支持配置
     */
    public enum Position {
        Left,
        right
    }
}
