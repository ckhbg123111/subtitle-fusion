package com.zhongjia.subtitlefusion.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SubtitleParserServiceTest {

    private SubtitleParserService parserService;
    private Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        parserService = new SubtitleParserService();
        tempDir = Files.createTempDirectory("srt-test-");
    }

    @AfterEach
    void tearDown() throws Exception {
        if (tempDir != null && Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .sorted((a, b) -> b.getNameCount() - a.getNameCount())
                    .forEach(p -> {
                        try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                    });
        }
    }

    @Test
    void parseSrt_utf8_ok() throws Exception {
        String srt = "1\n" +
                "00:00:01,000 --> 00:00:02,000\n" +
                "你好 世界\n\n" +
                "2\n" +
                "00:00:03,000 --> 00:00:04,500\n" +
                "Hello World\n";
        Path file = tempFile("sample_utf8.srt", srt, StandardCharsets.UTF_8);

        List<SubtitleParserService.SrtCue> cues = parserService.parseSrtFile(file);

        assertEquals(2, cues.size());
        assertEquals(1_000_000L, cues.get(0).startUs);
        assertEquals(2_000_000L, cues.get(0).endUs);
        assertEquals(List.of("你好 世界"), cues.get(0).lines);

        assertEquals(3_000_000L, cues.get(1).startUs);
        assertEquals(4_500_000L, cues.get(1).endUs);
        assertEquals(List.of("Hello World"), cues.get(1).lines);
    }

    @Test
    void parseSrt_nonUtf8_convertsToUtf8() throws Exception {
        String srt = "1\r\n" +
                "00:00:00,000 --> 00:00:01,000\r\n" +
                "中文测试\r\n\r\n" +
                "2\r\n" +
                "00:00:02,000 --> 00:00:03,000\r\n" +
                "更多内容\r\n";
        Path file = tempFile("gbk_sample.srt", srt, Charset.forName("GBK"));

        List<SubtitleParserService.SrtCue> cues = parserService.parseSrtFile(file);

        assertEquals(2, cues.size());
        assertEquals(List.of("中文测试"), cues.get(0).lines);
        assertEquals(List.of("更多内容"), cues.get(1).lines);
    }

    @Test
    void parseSrt_ignoresNoiseAndInvalidLines() throws Exception {
        String srt = "abc\n" +
                "1\n" +
                "00:00:00,500 --> 00:00:01,500\n" +
                "A\n" +
                "\n" +
                "garbage line\n" +
                "2\n" +
                "00:00:02,000 --> 00:00:03,000\n" +
                "B C\n";
        Path file = tempFile("noise.srt", srt, StandardCharsets.UTF_8);

        List<SubtitleParserService.SrtCue> cues = parserService.parseSrtFile(file);

        assertEquals(2, cues.size());
        assertEquals(500_000L, cues.get(0).startUs);
        assertEquals(1_500_000L, cues.get(0).endUs);
        assertEquals(List.of("A"), cues.get(0).lines);
        assertEquals(List.of("B C"), cues.get(1).lines);
    }

    private Path tempFile(String name, String content, Charset charset) throws Exception {
        Path f = tempDir.resolve(name);
        Files.writeString(f, content, charset);
        return f;
    }
}


