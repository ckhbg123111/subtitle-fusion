package com.zhongjia.subtitlefusion.model;

import lombok.Data;

/**
 * 漫剧关键帧定义（应用于段内 pictureUrl 图片的运动/动效）。
 * <p>
 * 若为空，服务端会按段索引轮换默认预设做兜底。
 */
@Data
public class WebtoonDramaKeyframeSpec {
    /**
     * 预设名称（可空）。建议值例如：P0_PAN_ZOOM / P1_VERTICAL_ZOOM_OUT / P2_ROTATE_SHAKE
     */
    private String preset;

    /**
     * 动效强度（可空，默认 1.0）。用于缩放/位移/旋转等幅度的倍率系数。
     */
    private Double strength;

    /**
     * 时间抖动（可空）。用于避免关键帧时间点与其他片段恰好重合导致的轨道状态串扰。
     */
    private Double jitterSec;
}


