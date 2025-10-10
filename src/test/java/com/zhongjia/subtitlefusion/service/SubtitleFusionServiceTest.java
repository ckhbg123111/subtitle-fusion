package com.zhongjia.subtitlefusion.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
// no ArgumentMatchers needed here
import static org.mockito.Mockito.*;

class SubtitleFusionServiceTest {

    @Mock
    private FileDownloadService downloadService;
    @Mock
    private SubtitleParserService parserService;
    @Mock
    private VideoProcessingService videoService;
    @Mock
    private MinioService minioService;

    private SubtitleFusionService fusionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        fusionService = new SubtitleFusionService(downloadService, parserService, videoService, minioService);
    }

    @Test
    void burnSrtViaJava2D_local_ok() throws Exception {
        Path videoPath = Path.of("C:/videos/input.mp4");
        Path srtPath = Path.of("C:/subs/track.srt");
        List<SubtitleParserService.SrtCue> cues = List.of(newCue(0, 1_000_000, "A"));

        when(parserService.parseSrtFile(srtPath)).thenReturn(cues);
        when(videoService.stripExt("input.mp4")).thenReturn("input");
        when(videoService.processVideoWithSubtitles(videoPath, cues, "input")).thenReturn("C:/out/input_sub_srt2d.mp4");

        String out = fusionService.burnSrtViaJava2D(videoPath, srtPath);

        assertEquals("C:/out/input_sub_srt2d.mp4", out);
        verify(parserService, times(1)).parseSrtFile(srtPath);
        verify(videoService, times(1)).processVideoWithSubtitles(videoPath, cues, "input");
        verifyNoInteractions(downloadService, minioService);
    }

    @Test
    void burnSrtViaJava2DFromUrls_ok() throws Exception {
        String videoUrl = "https://cdn.example.com/v/input.mp4?x=1";
        String srtUrl = "https://cdn.example.com/s/track.srt";
        Path tmpVideo = Path.of(System.getProperty("java.io.tmpdir"), "t-video.mp4");
        Path tmpSub = Path.of(System.getProperty("java.io.tmpdir"), "t-sub.srt");
        List<SubtitleParserService.SrtCue> cues = List.of(newCue(0, 1_000_000, "B"));
        Path rendered = Path.of(System.getProperty("java.io.tmpdir"), "out.mp4");

        when(downloadService.downloadVideo(videoUrl)).thenReturn(tmpVideo);
        when(downloadService.downloadSubtitle(srtUrl)).thenReturn(tmpSub);
        when(parserService.parseSrtFile(tmpSub)).thenReturn(cues);
        // baseName 从 URL 解析应为 input
        when(videoService.processVideoWithSubtitles(tmpVideo, cues, "input")).thenReturn(rendered.toString());

        when(minioService.uploadFile(rendered, "out.mp4")).thenReturn("videos/url-upload.mp4");

        String url = fusionService.burnSrtViaJava2DFromUrls(videoUrl, srtUrl);

        assertEquals("videos/url-upload.mp4", url);

        // 校验上传文件名来自输出路径文件名
        ArgumentCaptor<Path> pathCaptor = ArgumentCaptor.forClass(Path.class);
        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        verify(minioService).uploadFile(pathCaptor.capture(), nameCaptor.capture());
        assertEquals(rendered, pathCaptor.getValue());
        assertEquals("out.mp4", nameCaptor.getValue());

        // 校验清理
        verify(downloadService, atLeastOnce()).cleanupTempFile(tmpVideo);
        verify(downloadService, atLeastOnce()).cleanupTempFile(tmpSub);
    }

    @Test
    void burnSrtViaJava2DFromVideoUrlAndFile_ok() throws Exception {
        String videoUrl = "https://cdn.example.com/v/sample.mov";
        Path subtitlePath = Path.of("C:/subs/local.srt");
        Path tmpVideo = Path.of(System.getProperty("java.io.tmpdir"), "t-video2.mov");
        List<SubtitleParserService.SrtCue> cues = List.of(newCue(0, 1_000_000, "C"));
        Path rendered = Path.of(System.getProperty("java.io.tmpdir"), "out2.mp4");

        when(downloadService.downloadVideo(videoUrl)).thenReturn(tmpVideo);
        when(parserService.parseSrtFile(subtitlePath)).thenReturn(cues);
        // baseName 从 URL 解析应为 sample
        when(videoService.processVideoWithSubtitles(tmpVideo, cues, "sample")).thenReturn(rendered.toString());
        when(minioService.uploadFile(rendered, "out2.mp4")).thenReturn("videos/url-upload-2.mp4");

        String url = fusionService.burnSrtViaJava2DFromVideoUrlAndFile(videoUrl, subtitlePath);

        assertEquals("videos/url-upload-2.mp4", url);
        verify(downloadService, atLeastOnce()).cleanupTempFile(tmpVideo);
    }

    private SubtitleParserService.SrtCue newCue(long s, long e, String line) {
        SubtitleParserService.SrtCue c = new SubtitleParserService.SrtCue();
        c.startUs = s; c.endUs = e; c.lines = List.of(line);
        return c;
    }
}


