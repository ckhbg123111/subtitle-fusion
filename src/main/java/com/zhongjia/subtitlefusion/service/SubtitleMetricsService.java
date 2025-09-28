package com.zhongjia.subtitlefusion.service;

import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * 字幕指标计算服务
 * 计算在当前视频分辨率与默认字体设置下，每行建议的最大字符数（估算值）。
 * 不影响现有渲染逻辑，仅用于对外提供第三方换行参考。
 */
@Service
public class SubtitleMetricsService {

    /**
     * 计算建议的每行最大字数（中文、英文，及保守取最小）
     * - 与 SubtitleRendererService 的默认样式保持一致：
     *   左右安全边距 6%，字号 baseFontSize = max(18, round(h*0.035))
     */
    public LineCapacity calculateLineCapacity(int videoWidth, int videoHeight) {
        if (videoWidth <= 0 || videoHeight <= 0) {
            return new LineCapacity(0, 0, 0);
        }

        int marginH = Math.max(12, Math.round(videoWidth * 0.06f));
        int availableWidth = Math.max(1, videoWidth - marginH * 2);
        int fontSize = Math.max(18, Math.round(videoHeight * 0.035f));

        Font font = new Font("Microsoft YaHei", Font.PLAIN, fontSize);
        BufferedImage tmp = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = tmp.createGraphics();
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();

        // 中文估算：以“汉”字宽度为代表
        int wCjk = Math.max(1, fm.charWidth('汉'));
        int maxCjk = Math.max(1, (int)Math.floor(availableWidth / (double) wCjk));

        // 英文估算：对大小写字母取平均宽度
        String sample = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        int sum = 0;
        for (int i = 0; i < sample.length(); i++) sum += fm.charWidth(sample.charAt(i));
        double avgEn = Math.max(1.0, sum / (double) sample.length());
        int maxEn = Math.max(1, (int)Math.floor(availableWidth / avgEn));

        g.dispose();

        int conservative = Math.min(maxCjk, maxEn);
        return new LineCapacity(maxCjk, maxEn, conservative);
    }

    /**
     * 结果对象：包含中文估算、英文估算与保守值
     */
    public static class LineCapacity {
        public final int maxCharsChinese;
        public final int maxCharsEnglish;
        public final int conservative;

        public LineCapacity(int maxCharsChinese, int maxCharsEnglish, int conservative) {
            this.maxCharsChinese = maxCharsChinese;
            this.maxCharsEnglish = maxCharsEnglish;
            this.conservative = conservative;
        }
    }
}


