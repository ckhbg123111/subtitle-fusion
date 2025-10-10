package com.zhongjia.subtitlefusion.service;

import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * 字幕渲染服务
 * 负责将字幕绘制到视频帧上
 */
@Service
public class SubtitleRendererService {

    private final com.zhongjia.subtitlefusion.config.AppProperties appProperties;

    public SubtitleRendererService(com.zhongjia.subtitlefusion.config.AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    /**
     * 在图像上绘制SRT字幕
     * @param img 目标图像
     * @param timestampUs 当前时间戳（微秒）
     * @param cues 字幕条目列表
     */
    public void drawSrtOnImage(BufferedImage img, long timestampUs, List<SubtitleParserService.SrtCue> cues) {
        if (cues == null || cues.isEmpty()) return;
        
        int w = img.getWidth();
        int h = img.getHeight();
        Graphics2D g = img.createGraphics();
        
        try {
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int baseFontSize = Math.max(18, Math.round(h * 0.035f));
            int marginH = Math.max(12, Math.round(w * 0.06f));
            int marginBottom = Math.max(12, Math.round(h * 0.08f));
            int marginTop = Math.max(12, Math.round(h * 0.06f));
            int availableWidth = Math.max(1, w - marginH * 2);
            int availableHeight = Math.max(1, h - marginTop - marginBottom);

            for (SubtitleParserService.SrtCue cue : cues) {
                if (timestampUs < cue.startUs || timestampUs > cue.endUs) continue;
                
                // 自适应字号：按高度约束缩小字号
                // 解析样式配置
                com.zhongjia.subtitlefusion.config.AppProperties.Render render = appProperties.getRender();
                String family = render.getFontFamily() != null && !render.getFontFamily().isEmpty() ? render.getFontFamily() : "Microsoft YaHei";
                int style = Font.PLAIN;
                if (render.getFontStyle() != null) {
                    String s = render.getFontStyle();
                    if ("bold".equalsIgnoreCase(s)) style = Font.BOLD;
                    else if ("italic".equalsIgnoreCase(s)) style = Font.ITALIC;
                    else if ("bolditalic".equalsIgnoreCase(s) || "bold_italic".equalsIgnoreCase(s)) style = Font.BOLD | Font.ITALIC;
                }

                Integer minFontPxCfg = render.getMinFontSizePx();
                int minFontPx = (minFontPxCfg != null && minFontPxCfg > 0) ? minFontPxCfg : 14;

                Integer fontSizePxCfg = render.getFontSizePx();
                Float fontScaleCfg = render.getFontScale();

                int resolvedFontSize;
                if (fontSizePxCfg != null && fontSizePxCfg > 0) {
                    resolvedFontSize = fontSizePxCfg;
                } else if (fontScaleCfg != null && fontScaleCfg > 0) {
                    resolvedFontSize = Math.round(baseFontSize * fontScaleCfg);
                } else {
                    resolvedFontSize = baseFontSize;
                }
                resolvedFontSize = Math.max(resolvedFontSize, minFontPx);

                int fontSize = resolvedFontSize;
                FontMetrics fm;
                List<String> displayLines;
                int lineHeight;
                int totalHeight;
                while (true) {
                    Font font = new Font(family, style, fontSize);
                    g.setFont(font);
                    fm = g.getFontMetrics();
                    displayLines = new ArrayList<>();
                    for (String rawLine : cue.lines) {
                        if (rawLine == null) continue;
                        displayLines.addAll(wrapText(rawLine, fm, availableWidth));
                    }
                    lineHeight = fm.getHeight();
                    totalHeight = lineHeight * Math.max(1, displayLines.size());
                    if (totalHeight <= availableHeight || fontSize <= minFontPx) {
                        break;
                    }
                    fontSize -= 2;
                }

                int y = h - marginBottom - totalHeight;

                for (String line : displayLines) {
                    int textWidth = fm.stringWidth(line);
                    int x = Math.max(marginH, (w - textWidth) / 2);
                    
                    // 阴影描边
                    int alpha = 180;
                    Integer alphaCfg = render.getShadowAlpha();
                    if (alphaCfg != null && alphaCfg >= 0 && alphaCfg <= 255) alpha = alphaCfg;
                    int radius = 2;
                    Integer radiusCfg = render.getShadowRadiusPx();
                    if (radiusCfg != null && radiusCfg >= 0) radius = radiusCfg;

                    if (radius > 0 && alpha > 0) {
                        g.setColor(new Color(0, 0, 0, alpha));
                        for (int dx = -radius; dx <= radius; dx++)
                            for (int dy = -radius; dy <= radius; dy++)
                                if (dx != 0 || dy != 0) g.drawString(line, x + dx, y + dy);
                    }
                    
                    // 文字
                    g.setColor(Color.WHITE);
                    g.drawString(line, x, y);
                    y += lineHeight;
                }
            }
        } finally {
            g.dispose();
        }
    }

    /**
     * 将一行文本根据最大宽度进行自动换行（CJK逐字、含空格按词）。
     */
    private List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
        List<String> result = new ArrayList<>();
        if (text == null) return result;
        String s = text.replace("\r", "");
        if (s.isEmpty()) {
            result.add("");
            return result;
        }

        boolean hasSpace = s.indexOf(' ') >= 0;
        boolean likelyCjk = !hasSpace && containsCjk(s);

        if (likelyCjk) {
            StringBuilder line = new StringBuilder();
            for (int i = 0; i < s.length(); ) {
                int cp = s.codePointAt(i);
                String ch = new String(Character.toChars(cp));
                if (line.length() == 0) {
                    line.append(ch);
                } else {
                    String candidate = line + ch;
                    if (fm.stringWidth(candidate) <= maxWidth) {
                        line.append(ch);
                    } else {
                        result.add(line.toString());
                        line.setLength(0);
                        line.append(ch);
                    }
                }
                i += Character.charCount(cp);
            }
            if (line.length() > 0) result.add(line.toString());
        } else {
            String[] words = s.split(" ");
            StringBuilder line = new StringBuilder();
            for (String word : words) {
                if (line.length() == 0) {
                    if (fm.stringWidth(word) <= maxWidth) {
                        line.append(word);
                    } else {
                        // 单词本身超宽，退化为逐字切分
                        result.addAll(splitByChar(word, fm, maxWidth));
                    }
                } else {
                    String candidate = line + " " + word;
                    if (fm.stringWidth(candidate) <= maxWidth) {
                        line.append(" ").append(word);
                    } else {
                        result.add(line.toString());
                        line.setLength(0);
                        if (fm.stringWidth(word) <= maxWidth) {
                            line.append(word);
                        } else {
                            result.addAll(splitByChar(word, fm, maxWidth));
                        }
                    }
                }
            }
            if (line.length() > 0) result.add(line.toString());
        }

        return result;
    }

    /**
     * 将超宽的token按字符切分以适配宽度。
     */
    private List<String> splitByChar(String token, FontMetrics fm, int maxWidth) {
        List<String> parts = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < token.length(); ) {
            int cp = token.codePointAt(i);
            String ch = new String(Character.toChars(cp));
            if (line.length() == 0) {
                line.append(ch);
            } else {
                String candidate = line + ch;
                if (fm.stringWidth(candidate) <= maxWidth) {
                    line.append(ch);
                } else {
                    parts.add(line.toString());
                    line.setLength(0);
                    line.append(ch);
                }
            }
            i += Character.charCount(cp);
        }
        if (line.length() > 0) parts.add(line.toString());
        return parts;
    }

    /**
     * 粗略判定是否包含CJK字符。
     */
    private boolean containsCjk(String s) {
        for (int i = 0; i < s.length(); ) {
            int cp = s.codePointAt(i);
            Character.UnicodeScript script = Character.UnicodeScript.of(cp);
            if (script == Character.UnicodeScript.HAN ||
                script == Character.UnicodeScript.HIRAGANA ||
                script == Character.UnicodeScript.KATAKANA ||
                script == Character.UnicodeScript.HANGUL) {
                return true;
            }
            i += Character.charCount(cp);
        }
        return false;
    }
}
