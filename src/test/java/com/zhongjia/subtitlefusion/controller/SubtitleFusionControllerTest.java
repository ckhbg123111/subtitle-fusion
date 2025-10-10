package com.zhongjia.subtitlefusion.controller;

import com.zhongjia.subtitlefusion.model.TaskInfo;
import com.zhongjia.subtitlefusion.model.TaskState;
import com.zhongjia.subtitlefusion.service.AsyncSubtitleFusionService;
import com.zhongjia.subtitlefusion.service.DistributedTaskManagementService;
import com.zhongjia.subtitlefusion.service.MinioService;
import com.zhongjia.subtitlefusion.service.SubtitleFusionService;
import com.zhongjia.subtitlefusion.service.SubtitleMetricsService;
import io.minio.GetObjectResponse;
import io.minio.StatObjectResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SubtitleFusionControllerTest {

    @Mock
    private SubtitleFusionService fusionService;
    @Mock
    private AsyncSubtitleFusionService asyncFusionService;
    @Mock
    private DistributedTaskManagementService taskManagementService;
    @Mock
    private MinioService minioService;
    @Mock
    private SubtitleMetricsService subtitleMetricsService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SubtitleFusionController controller = new SubtitleFusionController(
                fusionService,
                asyncFusionService,
                taskManagementService,
                minioService,
                java.util.Optional.empty(),
                subtitleMetricsService
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getLineCapacity_ok() throws Exception {
        SubtitleMetricsService.LineCapacity cap = new SubtitleMetricsService.LineCapacity(20, 40, 18);
        when(subtitleMetricsService.calculateLineCapacityWithOptions(anyInt(), anyInt(), any())).thenReturn(cap);

        mockMvc.perform(get("/api/subtitles/line-capacity")
                        .param("width", "1920")
                        .param("height", "1080")
                        .param("fontScale", "1.2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.videoWidth", is(1920)))
                .andExpect(jsonPath("$.videoHeight", is(1080)))
                .andExpect(jsonPath("$.maxCharsChinese", is(20)))
                .andExpect(jsonPath("$.maxCharsEnglish", is(40)))
                .andExpect(jsonPath("$.conservative", is(18)))
                .andExpect(jsonPath("$.fontSizePx", greaterThan(0)))
                .andExpect(jsonPath("$.fontFamily", notNullValue()));
    }

    @Test
    void submitAsyncTask_paramValidation() throws Exception {
        MockMultipartFile subFile = new MockMultipartFile("subtitleFile", "a.txt", "text/plain", "bad".getBytes());
        mockMvc.perform(multipart("/api/subtitles/burn-url-srt/async")
                        .file(subFile)
                        .param("taskId", "")
                        .param("videoUrl", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("不能为空")));
    }

    @Test
    void submitAsyncTask_success() throws Exception {
        when(taskManagementService.taskExists("t1")).thenReturn(false);
        TaskInfo created = new TaskInfo("t1");
        when(taskManagementService.createTask("t1")).thenReturn(created);

        MockMultipartFile subFile = new MockMultipartFile("subtitleFile", "x.srt", "text/plain", "1".getBytes());

        mockMvc.perform(multipart("/api/subtitles/burn-url-srt/async")
                        .file(subFile)
                        .param("taskId", "t1")
                        .param("videoUrl", "https://example.com/a.mp4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId", is("t1")))
                .andExpect(jsonPath("$.state", is("PENDING")));

        verify(asyncFusionService).processVideoUrlWithSubtitleFileAsync(eq("t1"), eq("https://example.com/a.mp4"), any());
    }

    @Test
    void submitAsyncTask_duplicateTaskId_or_invalidUrl_or_badExt() throws Exception {
        // 重复taskId
        when(taskManagementService.taskExists("dup")).thenReturn(true);
        MockMultipartFile srt = new MockMultipartFile("subtitleFile", "x.srt", "text/plain", new byte[]{1});
        mockMvc.perform(multipart("/api/subtitles/burn-url-srt/async")
                        .file(srt)
                        .param("taskId", "dup")
                        .param("videoUrl", "https://ok"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("已存在")));

        // URL非法
        when(taskManagementService.taskExists("t2")).thenReturn(false);
        mockMvc.perform(multipart("/api/subtitles/burn-url-srt/async")
                        .file(srt)
                        .param("taskId", "t2")
                        .param("videoUrl", "ftp://bad"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("无效的URL")));

        // 字幕后缀非法
        MockMultipartFile badExt = new MockMultipartFile("subtitleFile", "x.txt", "text/plain", new byte[]{1});
        mockMvc.perform(multipart("/api/subtitles/burn-url-srt/async")
                        .file(badExt)
                        .param("taskId", "t3")
                        .param("videoUrl", "https://ok"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("仅支持 .srt")));
    }

    @Test
    void getTaskStatus_notFound() throws Exception {
        when(taskManagementService.getTask("none")).thenReturn(null);
        mockMvc.perform(get("/api/subtitles/task/none"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId", is("none")))
                .andExpect(jsonPath("$.message", containsString("不存在")));
    }

    @Test
    void getTaskStatus_ok() throws Exception {
        TaskInfo t = new TaskInfo("t2");
        t.updateProgress(TaskState.PROCESSING, 50, "处理中");
        when(taskManagementService.getTask("t2")).thenReturn(t);
        mockMvc.perform(get("/api/subtitles/task/t2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId", is("t2")))
                .andExpect(jsonPath("$.state", is("PROCESSING")))
                .andExpect(jsonPath("$.progress", is(50)));
    }

    @Test
    void download_minio_full_and_range() throws Exception {
        // full
        StatObjectResponse stat = mock(StatObjectResponse.class);
        when(stat.contentType()).thenReturn("video/mp4");
        when(stat.size()).thenReturn(100L);
        when(minioService.statObject("videos/a.mp4")).thenReturn(stat);

        GetObjectResponse obj = mock(GetObjectResponse.class);
        when(minioService.getObject("videos/a.mp4")).thenReturn(obj);

        mockMvc.perform(get("/api/subtitles/download").param("path", "videos/a.mp4"))
                .andExpect(status().isOk())
                .andExpect(header().string("Accept-Ranges", "bytes"))
                .andExpect(header().string("Content-Disposition", containsString("inline")))
                .andExpect(content().contentType("video/mp4"));

        // range
        GetObjectResponse rangeObj = mock(GetObjectResponse.class);
        when(minioService.getObjectRange("videos/a.mp4", 0L, 10L)).thenReturn(rangeObj);

        mockMvc.perform(get("/api/subtitles/download")
                        .param("path", "videos/a.mp4")
                        .header("Range", "bytes=0-9"))
                .andExpect(status().isPartialContent())
                .andExpect(header().string("Content-Range", containsString("bytes 0-9/100")));
    }

    @Test
    void download_invalid_path_or_range() throws Exception {
        // 路径非法
        mockMvc.perform(get("/api/subtitles/download").param("path", "../secret"))
                .andExpect(status().isBadRequest());

        // 无效Range
        StatObjectResponse stat = mock(StatObjectResponse.class);
        when(stat.contentType()).thenReturn("video/mp4");
        when(stat.size()).thenReturn(100L);
        when(minioService.statObject("videos/a.mp4")).thenReturn(stat);

        mockMvc.perform(get("/api/subtitles/download")
                        .param("path", "videos/a.mp4")
                        .header("Range", "bytes=abc-def"))
                .andExpect(status().isRequestedRangeNotSatisfiable());
    }
}


