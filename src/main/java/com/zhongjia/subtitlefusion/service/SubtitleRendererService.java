package com.zhongjia.subtitlefusion.service;

import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * 字幕渲染服务
 * 负责将字幕绘制到视频帧上
 */
@Service
public class SubtitleRendererService {

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
            int fontSize = Math.max(18, Math.round(h * 0.035f));
            Font font = new Font("Microsoft YaHei", Font.PLAIN, fontSize);
            g.setFont(font);

            for (SubtitleParserService.SrtCue cue : cues) {
                if (timestampUs < cue.startUs || timestampUs > cue.endUs) continue;
                
                // 计算文本块高度
                int lineHeight = g.getFontMetrics().getHeight();
                int totalHeight = lineHeight * cue.lines.size();
                int y = h - Math.round(h * 0.08f) - totalHeight; // 距底部 8% 留白

                for (String line : cue.lines) {
                    int textWidth = g.getFontMetrics().stringWidth(line);
                    int x = (w - textWidth) / 2;
                    
                    // 阴影描边
                    g.setColor(new Color(0, 0, 0, 180));
                    for (int dx = -2; dx <= 2; dx++)
                        for (int dy = -2; dy <= 2; dy++)
                            if (dx != 0 || dy != 0) g.drawString(line, x + dx, y + dy);
                    
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
}
