package com.zhongjia.subtitlefusion.model;

import lombok.Data;

import java.util.List;

@Data
public class SubtitleFusionV2Request {
    private String taskId;
    private String videoUrl;
    private SubtitleInfo subtitleInfo;
    private List<PictureInfo> pictureInfoList;

    /**
     * 是否云渲染
     */
    private Boolean cloudRendering;




}