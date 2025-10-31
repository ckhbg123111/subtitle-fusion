package com.zhongjia.subtitlefusion.util;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 服务端文本排版工具：在给定矩形内进行自动换行并自适应字号。
 */
public final class TextLayoutUtils {

    private TextLayoutUtils() {}

    public static final class Result {
        public final String textWithNewlines;
        public final int fontSize;
        public final int lineSpacing;

        public Result(String textWithNewlines, int fontSize, int lineSpacing) {
            this.textWithNewlines = textWithNewlines;
            this.fontSize = fontSize;
            this.lineSpacing = lineSpacing;
        }
    }

    /**
     * 在指定矩形内进行换行与字号自适应。
     * @param rawText 原始文本
     * @param maxWidth 最大宽度（像素）
     * @param maxHeight 最大高度（像素）
     * @param fontFile 字体文件路径（TTF/OTF）
     * @param minSize 最小字号（含）
     * @param maxSize 最大字号（含）
     * @param lineSpacing 行距（像素）
     */
    public static Result layout(String rawText,
                                int maxWidth,
                                int maxHeight,
                                Path fontFile,
                                int minSize,
                                int maxSize,
                                int lineSpacing) throws Exception {
        if (rawText == null) rawText = "";
        if (minSize < 1) minSize = 1;
        if (maxSize < minSize) maxSize = minSize;

        System.setProperty("java.awt.headless", "true");

        Font base = Font.createFont(Font.TRUETYPE_FONT, fontFile.toFile());
        int lo = minSize, hi = maxSize;
        int best = minSize;

        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            Font f = base.deriveFont((float) mid);
            List<String> lines = wrap(rawText, f, maxWidth);
            int totalHeight = lines.isEmpty() ? mid : (lines.size() * mid + (lines.size() - 1) * lineSpacing);
            if (totalHeight <= maxHeight) {
                best = mid;
                lo = mid + 1; // 尝试更大字号
            } else {
                hi = mid - 1;
            }
        }

        // 最终以 best 再计算一次，保证文本为对应换行
        Font fBest = base.deriveFont((float) best);
        List<String> linesBest = wrap(rawText, fBest, maxWidth);
        String text = String.join("\n", linesBest);
        return new Result(text, best, lineSpacing);
    }

    /**
     * 简单贪心换行：按字符累计宽度，超过上限时换行。
     * 英文优先按空格断行；若单词过长，则退化到字符级断行。
     */
    private static List<String> wrap(String s, Font font, int maxWidth) {
        FontRenderContext frc = new FontRenderContext(null, true, true);
        List<String> out = new ArrayList<>();
        if (s.isEmpty()) { out.add(""); return out; }

        String[] paragraphs = s.split("\r?\n");
        for (String para : paragraphs) {
            if (para.isEmpty()) { out.add(""); continue; }

            int i = 0;
            while (i < para.length()) {
                // 尝试按空格扩展一行
                int lastFitting = i;
                int j = i;
                while (j <= para.length()) {
                    String candidate = para.substring(i, j);
                    Rectangle2D bounds = font.getStringBounds(candidate, frc);
                    if (bounds.getWidth() <= maxWidth) {
                        lastFitting = j;
                        if (j == para.length()) break;
                        // 前进到下一个单词边界（空格）
                        int nextSpace = nextBreakPos(para, j);
                        j = Math.max(j + 1, nextSpace);
                    } else {
                        break;
                    }
                }

                if (lastFitting == i) {
                    // 单词/字符过长，退化到逐字符
                    int k = i + 1;
                    while (k <= para.length()) {
                        String cand = para.substring(i, k);
                        Rectangle2D b = font.getStringBounds(cand, frc);
                        if (b.getWidth() <= maxWidth) { k++; }
                        else { k--; break; }
                    }
                    if (k <= i) k = i + 1;
                    out.add(para.substring(i, k));
                    i = k;
                } else {
                    // 去除右侧多余空格
                    String line = para.substring(i, lastFitting).replaceAll("\\s+$", "");
                    out.add(line);
                    i = lastFitting;
                }
            }
        }
        return out;
    }

    private static int nextBreakPos(String s, int from) {
        int space = s.indexOf(' ', from);
        return space < 0 ? from + 1 : space + 1;
    }
}


