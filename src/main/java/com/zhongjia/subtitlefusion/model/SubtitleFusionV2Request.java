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

    /**
     * 可选：视频分辨率（像素）。
     * - 当两者均为正数时，将直接使用该分辨率；
     * - 当任一为空或非正数时，系统将自动探测视频分辨率。
     */
    private Integer videoWidth;
    private Integer videoHeight;




}