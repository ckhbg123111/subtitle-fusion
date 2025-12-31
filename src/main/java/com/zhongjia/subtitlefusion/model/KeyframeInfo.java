package com.zhongjia.subtitlefusion.model;

import lombok.Data;

@Data
public class KeyframeInfo {
    /**
     * 关键帧图片（必填，其他选填）
     */
    private String pictureUrl;
    /**
     * 关键帧定义
     */
    private WebtoonDramaKeyframeSpec keyframeSpec;
    /**
     * 入场动销
     */
    private String intro;
    /**
     * 出场动效
     */
    private String outro;
    /**
     * 循环动效
     */
    private String combo;
}
