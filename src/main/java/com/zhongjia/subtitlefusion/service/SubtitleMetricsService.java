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

    /**
     * 可选参数版本：允许覆盖字体、字号与边距等，提供更高自由度。
     */
    public LineCapacity calculateLineCapacityWithOptions(int videoWidth, int videoHeight, Options options) {
        if (videoWidth <= 0 || videoHeight <= 0) {
            return new LineCapacity(0, 0, 0);
        }

        Options opt = options != null ? options : new Options();

        // 解析边距：优先像素值，其次百分比，默认6%
        Integer marginHpx = opt.marginHpx;
        Float marginHPercent = opt.marginHPercent;
        int marginH;
        if (marginHpx != null && marginHpx >= 0) {
            marginH = marginHpx;
        } else if (marginHPercent != null && marginHPercent >= 0) {
            marginH = Math.round(videoWidth * (marginHPercent / 100f));
        } else {
            marginH = Math.max(12, Math.round(videoWidth * 0.06f));
        }

        int availableWidth = Math.max(1, videoWidth - marginH * 2);

        // 字号：可直接指定像素，或在默认基础上乘以scale，且不小于min
        int baseFont = Math.max(18, Math.round(videoHeight * 0.035f));
        int fontSize;
        if (opt.fontSizePx != null && opt.fontSizePx > 0) {
            fontSize = opt.fontSizePx;
        } else if (opt.fontScale != null && opt.fontScale > 0) {
            fontSize = Math.round(baseFont * opt.fontScale);
        } else {
            fontSize = baseFont;
        }
        if (opt.minFontSizePx != null && opt.minFontSizePx > 0) {
            fontSize = Math.max(fontSize, opt.minFontSizePx);
        }

        // 字体与样式
        String family = opt.fontFamily != null && !opt.fontFamily.isEmpty() ? opt.fontFamily : "Microsoft YaHei";
        int style = Font.PLAIN;
        if (opt.fontStyle != null) {
            if ("bold".equalsIgnoreCase(opt.fontStyle)) style = Font.BOLD;
            else if ("italic".equalsIgnoreCase(opt.fontStyle)) style = Font.ITALIC;
            else if ("bolditalic".equalsIgnoreCase(opt.fontStyle) || "bold_italic".equalsIgnoreCase(opt.fontStyle)) style = Font.BOLD | Font.ITALIC;
        }

        Font font = new Font(family, style, fontSize);
        BufferedImage tmp = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = tmp.createGraphics();
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();

        // 中文估算字符：允许自定义代表字符
        char cjkChar = opt.cjkChar != null ? opt.cjkChar : '汉';
        int wCjk = Math.max(1, fm.charWidth(cjkChar));
        int maxCjk = Math.max(1, (int)Math.floor(availableWidth / (double) wCjk));

        // 英文样本：可自定义
        String sample = (opt.englishSample != null && !opt.englishSample.isEmpty()) ? opt.englishSample : "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        int sum = 0;
        for (int i = 0; i < sample.length(); i++) sum += fm.charWidth(sample.charAt(i));
        double avgEn = Math.max(1.0, sum / (double) sample.length());
        int maxEn = Math.max(1, (int)Math.floor(availableWidth / avgEn));

        g.dispose();

        int conservative;
        if (opt.strategy != null && opt.strategy.equalsIgnoreCase("english")) conservative = maxEn;
        else if (opt.strategy != null && opt.strategy.equalsIgnoreCase("chinese")) conservative = maxCjk;
        else conservative = Math.min(maxCjk, maxEn);

        return new LineCapacity(maxCjk, maxEn, conservative);
    }

    /**
     * 计算可选项
     */
    public static class Options {
        public String fontFamily;       // 字体族，默认 Microsoft YaHei
        public String fontStyle;        // plain|bold|italic|bolditalic
        public Integer fontSizePx;      // 直接指定字号像素
        public Float fontScale;         // 在默认字号基础上的比例
        public Integer minFontSizePx;   // 最小字号
        public Integer marginHpx;       // 左右边距（像素）
        public Float marginHPercent;    // 左右边距（百分比 0-100）
        public Character cjkChar;       // CJK代表字符
        public String englishSample;    // 英文样本集
        public String strategy;         // conservative取值策略：default|min|chinese|english
    }
}


