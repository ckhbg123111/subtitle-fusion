package com.zhongjia.subtitlefusion.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 对外返回的每行容量估算响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
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

    // 兼容旧用法的构造器（仅核心5字段）
    public LineCapacityResponse(int videoWidth, int videoHeight,
                                int maxCharsChinese, int maxCharsEnglish, int conservative) {
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
        this.maxCharsChinese = maxCharsChinese;
        this.maxCharsEnglish = maxCharsEnglish;
        this.conservative = conservative;
    }

    // 兼容旧用法的流式设置方法
    public LineCapacityResponse withOptionsEcho(String fontFamily, String fontStyle, int fontSizePx, int marginHpx, String strategy) {
        this.fontFamily = fontFamily;
        this.fontStyle = fontStyle;
        this.fontSizePx = fontSizePx;
        this.marginHpx = marginHpx;
        this.strategy = strategy;
        return this;
    }
}
