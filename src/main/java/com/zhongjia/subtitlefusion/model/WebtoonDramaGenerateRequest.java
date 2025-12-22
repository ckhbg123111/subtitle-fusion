package com.zhongjia.subtitlefusion.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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
    @JsonDeserialize(using = SubtitleTemplateLenientDeserializer.class)
    private SubtitleTemplate subtitleTemplate;

    /**
     * 是否除法云渲染
     */
    private Boolean cloudRendering;
}
