package com.zhongjia.subtitlefusion.model;

/**
 * 对外返回的每行容量估算响应
 */
public class LineCapacityResponse {
    private int videoWidth;
    private int videoHeight;
    private int maxCharsChinese;
    private int maxCharsEnglish;
    private int conservative;

    public LineCapacityResponse(int videoWidth, int videoHeight,
                                int maxCharsChinese, int maxCharsEnglish, int conservative) {
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
        this.maxCharsChinese = maxCharsChinese;
        this.maxCharsEnglish = maxCharsEnglish;
        this.conservative = conservative;
    }

    public int getVideoWidth() { return videoWidth; }
    public int getVideoHeight() { return videoHeight; }
    public int getMaxCharsChinese() { return maxCharsChinese; }
    public int getMaxCharsEnglish() { return maxCharsEnglish; }
    public int getConservative() { return conservative; }
}


