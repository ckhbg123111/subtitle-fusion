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

    // 回显解析后的可选参数
    private String fontFamily;
    private String fontStyle;
    private int fontSizePx;
    private int marginHpx;
    private String strategy;

    public LineCapacityResponse(int videoWidth, int videoHeight,
                                int maxCharsChinese, int maxCharsEnglish, int conservative) {
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
        this.maxCharsChinese = maxCharsChinese;
        this.maxCharsEnglish = maxCharsEnglish;
        this.conservative = conservative;
    }

    public LineCapacityResponse withOptionsEcho(String fontFamily, String fontStyle, int fontSizePx, int marginHpx, String strategy) {
        this.fontFamily = fontFamily;
        this.fontStyle = fontStyle;
        this.fontSizePx = fontSizePx;
        this.marginHpx = marginHpx;
        this.strategy = strategy;
        return this;
    }

    public int getVideoWidth() { return videoWidth; }
    public int getVideoHeight() { return videoHeight; }
    public int getMaxCharsChinese() { return maxCharsChinese; }
    public int getMaxCharsEnglish() { return maxCharsEnglish; }
    public int getConservative() { return conservative; }

    public String getFontFamily() { return fontFamily; }
    public String getFontStyle() { return fontStyle; }
    public int getFontSizePx() { return fontSizePx; }
    public int getMarginHpx() { return marginHpx; }
    public String getStrategy() { return strategy; }
}


