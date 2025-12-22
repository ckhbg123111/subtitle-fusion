package com.zhongjia.subtitlefusion.model;

import lombok.Data;

import java.util.List;

@Data
public class WebtoonDramaGenerateRequest {
    /**
     * 段信息
     */
    List<WebtoonDramaSegmentInfo> segment;
    /**
     * 字幕模板信息
     */
    private SubtitleTemplate subtitleTemplate;

    /**
     * 是否除法云渲染
     */
    private Boolean cloudRendering;
}
