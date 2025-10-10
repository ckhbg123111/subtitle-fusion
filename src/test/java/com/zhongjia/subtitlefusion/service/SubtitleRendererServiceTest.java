package com.zhongjia.subtitlefusion.service;

import com.zhongjia.subtitlefusion.config.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SubtitleRendererServiceTest {

    private SubtitleRendererService renderer;

    @BeforeEach
    void setUp() {
        AppProperties props = new AppProperties();
        // 使用较小的阴影半径，便于像素对比
        props.getRender().setShadowRadiusPx(2);
        props.getRender().setShadowAlpha(180);
        renderer = new SubtitleRendererService(props);
    }

    @Test
    void drawSrtOnImage_noCues_noChange() {
        BufferedImage img = new BufferedImage(640, 360, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        g.dispose();

        int[] before = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
        renderer.drawSrtOnImage(img, 1_000_000L, List.of());
        int[] after = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());

        assertArrayEquals(before, after, "无字幕时不应改变像素");
    }

    @Test
    void drawSrtOnImage_outOfRange_noChange() {
        BufferedImage img = new BufferedImage(640, 360, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        g.dispose();

        SubtitleParserService.SrtCue cue = new SubtitleParserService.SrtCue();
        cue.startUs = 2_000_000L;
        cue.endUs = 3_000_000L;
        cue.lines = List.of("测试");

        int[] before = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
        renderer.drawSrtOnImage(img, 1_000_000L, List.of(cue));
        int[] after = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());

        assertArrayEquals(before, after, "时间窗外不应绘制");
    }

    @Test
    void drawSrtOnImage_inRange_shouldDraw() {
        BufferedImage img = new BufferedImage(640, 360, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        g.dispose();

        SubtitleParserService.SrtCue cue = new SubtitleParserService.SrtCue();
        cue.startUs = 500_000L;
        cue.endUs = 2_000_000L;
        cue.lines = List.of("Hello 世界");

        int[] before = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
        renderer.drawSrtOnImage(img, 1_000_000L, List.of(cue));
        int[] after = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());

        assertTrue(pixelChanged(before, after), "应当有像素被修改（绘制了字幕）");
    }

    private boolean pixelChanged(int[] before, int[] after) {
        if (before.length != after.length) return true;
        for (int i = 0; i < before.length; i++) {
            if (before[i] != after[i]) return true;
        }
        return false;
    }
}


