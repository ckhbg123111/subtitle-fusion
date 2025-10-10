package com.zhongjia.subtitlefusion.service;

import com.zhongjia.subtitlefusion.model.TaskState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AsyncSubtitleFusionServiceTest {

    @Mock
    private DistributedTaskManagementService taskManagementService;
    @Mock
    private FileDownloadService downloadService;
    @Mock
    private SubtitleParserService parserService;
    @Mock
    private VideoProcessingService videoService;
    @Mock
    private MinioService minioService;

    private AsyncSubtitleFusionService asyncService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        asyncService = new AsyncSubtitleFusionService(taskManagementService, downloadService, parserService, videoService, minioService);
    }

    @Test
    void processVideoUrlWithSubtitleFileAsync_ok_and_cleanup() throws Exception {
        String taskId = "t-1";
        String videoUrl = "https://cdn.example.com/v/in.mp4";
        Path subPath = Files.createTempFile("sub-", ".srt");

        // 创建可被删除的真实临时文件用于验证清理
        Path tmpVideo = Files.createTempFile("vid-", ".mp4");
        Path rendered = Files.createTempFile("out-", ".mp4");

        List<SubtitleParserService.SrtCue> cues = List.of(newCue(0, 1_000_000, "X"));

        when(downloadService.downloadVideo(videoUrl)).thenReturn(tmpVideo);
        when(parserService.parseSrtFile(subPath)).thenReturn(cues);
        when(videoService.processVideoWithSubtitles(tmpVideo, cues, "in")).thenReturn(rendered.toString());

        when(minioService.uploadFile(rendered, rendered.getFileName().toString())).thenReturn("videos/async.mp4");

        asyncService.processVideoUrlWithSubtitleFileAsync(taskId, videoUrl, subPath).join();

        verify(taskManagementService, atLeastOnce()).updateTaskProgress(eq(taskId), any(TaskState.class), anyInt(), anyString());
        verify(taskManagementService).markTaskCompleted(taskId, "videos/async.mp4");

        // 验证本地临时文件被删除
        assertFalse(Files.exists(tmpVideo), "下载的视频临时文件应被清理");
        assertFalse(Files.exists(rendered), "输出的本地文件应被清理");
    }

    private SubtitleParserService.SrtCue newCue(long s, long e, String line) {
        SubtitleParserService.SrtCue c = new SubtitleParserService.SrtCue();
        c.startUs = s; c.endUs = e; c.lines = List.of(line);
        return c;
    }
}


