package com.zhongjia.subtitlefusion.service;

import com.zhongjia.subtitlefusion.model.TaskState;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 异步字幕融合服务
 * 负责在后台异步处理字幕渲染任务
 */
@Service
public class AsyncSubtitleFusionService {

    private final DistributedTaskManagementService taskManagementService;
    private final FileDownloadService downloadService;
    private final SubtitleParserService parserService;
    private final VideoProcessingService videoService;
    private final MinioService minioService;

    public AsyncSubtitleFusionService(DistributedTaskManagementService taskManagementService,
                                     FileDownloadService downloadService,
                                     SubtitleParserService parserService,
                                     VideoProcessingService videoService,
                                     MinioService minioService) {
        this.taskManagementService = taskManagementService;
        this.downloadService = downloadService;
        this.parserService = parserService;
        this.videoService = videoService;
        this.minioService = minioService;
    }

    /**
     * 异步处理视频URL + 字幕文件的合成任务
     */
    @Async("subtitleTaskExecutor")
    public CompletableFuture<Void> processVideoUrlWithSubtitleFileAsync(String taskId, String videoUrl, Path subtitlePath) {
        Path tempVideoPath = null;
        Path outputVideoPath = null;

        try {
            // 1. 下载视频阶段
            taskManagementService.updateTaskProgress(taskId, TaskState.DOWNLOADING, 10, "开始下载视频文件");
            tempVideoPath = downloadService.downloadVideo(videoUrl);
            taskManagementService.updateTaskProgress(taskId, TaskState.DOWNLOADING, 25, "视频下载完成");

            // 2. 解析字幕阶段
            taskManagementService.updateTaskProgress(taskId, TaskState.PROCESSING, 30, "解析字幕文件");
            List<SubtitleParserService.SrtCue> cues = parserService.parseSrtFile(subtitlePath);
            taskManagementService.updateTaskProgress(taskId, TaskState.PROCESSING, 35, "字幕解析完成");

            // 3. 视频处理阶段
            taskManagementService.updateTaskProgress(taskId, TaskState.PROCESSING, 40, "开始渲染字幕到视频");
            String baseName = extractFileNameFromUrl(videoUrl);
            
            // 创建一个自定义的VideoProcessingService来支持进度回调
            String outputPath = processVideoWithProgress(taskId, tempVideoPath, cues, baseName);
            outputVideoPath = Path.of(outputPath);
            
            taskManagementService.updateTaskProgress(taskId, TaskState.PROCESSING, 80, "字幕渲染完成");

            // 4. 上传到MinIO阶段
            taskManagementService.updateTaskProgress(taskId, TaskState.UPLOADING, 85, "上传到MinIO");
            String fileName = outputVideoPath.getFileName().toString();
            String minioUrl = minioService.uploadFile(outputVideoPath, fileName);
            taskManagementService.updateTaskProgress(taskId, TaskState.UPLOADING, 95, "上传完成");

            // 5. 完成
            taskManagementService.markTaskCompleted(taskId, minioUrl);

        } catch (Exception e) {
            String errorMessage = "处理失败: " + e.getMessage();
            taskManagementService.markTaskFailed(taskId, errorMessage);
            System.err.println("任务 " + taskId + " 处理失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 清理临时文件
            cleanupFiles(tempVideoPath, outputVideoPath);
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * 带进度回调的视频处理
     */
    private String processVideoWithProgress(String taskId, Path videoPath, 
                                          List<SubtitleParserService.SrtCue> cues, 
                                          String baseName) throws Exception {
        // 这里可以在VideoProcessingService中添加进度回调接口
        // 目前先使用原有的处理方法，后续可以扩展
        taskManagementService.updateTaskProgress(taskId, TaskState.PROCESSING, 50, "正在逐帧渲染字幕");
        String result = videoService.processVideoWithSubtitles(videoPath, cues, baseName);
        taskManagementService.updateTaskProgress(taskId, TaskState.PROCESSING, 75, "视频渲染接近完成");
        return result;
    }

    /**
     * 从URL中提取文件名作为基础名称
     */
    private String extractFileNameFromUrl(String url) {
        try {
            // 移除查询参数
            String path = url.split("\\?")[0];
            String fileName = path.substring(path.lastIndexOf('/') + 1);
            return videoService.stripExt(fileName);
        } catch (Exception e) {
            return "video_from_url";
        }
    }

    /**
     * 清理临时文件
     */
    private void cleanupFiles(Path... paths) {
        for (Path path : paths) {
            if (path != null && Files.exists(path)) {
                try {
                    Files.delete(path);
                    System.out.println("已清理临时文件: " + path);
                } catch (Exception e) {
                    System.err.println("清理临时文件失败: " + path + ", 错误: " + e.getMessage());
                }
            }
        }
    }
}
