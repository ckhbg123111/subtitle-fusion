package com.zhongjia.subtitlefusion.model;

import lombok.Data;

@Data
public class PictureInfo {

    private String pictureUrl;
    private String startTime;
    private String endTime;
    // 图片入场动效
    private String imageIntro;
    // 图片出场动效
    private String imageOutro;
    // 插图入场音效
    private String effectAudioUrl;
    // 位置 LEFT、 RIGHT
    private String position;
}
