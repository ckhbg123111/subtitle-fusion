package com.zhongjia.subtitlefusion.controller;

import com.zhongjia.subtitlefusion.model.SubtitleFusionLocalRequest;
import com.zhongjia.subtitlefusion.model.SubtitleFusionResponse;
import com.zhongjia.subtitlefusion.model.TaskInfo;
import com.zhongjia.subtitlefusion.model.TaskResponse;
import com.zhongjia.subtitlefusion.service.AsyncSubtitleFusionService;
import com.zhongjia.subtitlefusion.service.DistributedTaskManagementService;
import com.zhongjia.subtitlefusion.service.HealthCheckService;
import com.zhongjia.subtitlefusion.service.SubtitleFusionService;
import com.zhongjia.subtitlefusion.service.MinioService;
import io.minio.GetObjectResponse;
import io.minio.StatObjectResponse;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@RestController
@RequestMapping("/api/subtitles")
public class SubtitleFusionController {

    private final SubtitleFusionService fusionService;
    private final AsyncSubtitleFusionService asyncFusionService;
    private final DistributedTaskManagementService taskManagementService;
    private final MinioService minioService;
    private final HealthCheckService healthCheckService;

    public SubtitleFusionController(SubtitleFusionService fusionService,
                                   AsyncSubtitleFusionService asyncFusionService,
                                   DistributedTaskManagementService taskManagementService,
                                   MinioService minioService,
                                   Optional<HealthCheckService> healthCheckService) {
        this.fusionService = fusionService;
        this.asyncFusionService = asyncFusionService;
        this.taskManagementService = taskManagementService;
        this.minioService = minioService;
        this.healthCheckService = healthCheckService.orElse(null);
    }

    /**
     * Java2D 字幕渲染方案 - 稳定可靠，完全不依赖FFmpeg滤镜
     * 支持：SRT格式，自动编码处理，中文字幕优化
     */
    @PostMapping(value = "/burn-local-srt", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public SubtitleFusionResponse burnLocalSrt(@RequestBody SubtitleFusionLocalRequest req) throws Exception {
        if (req == null || !StringUtils.hasText(req.getVideoPath()) || !StringUtils.hasText(req.getSubtitlePath())) {
            return new SubtitleFusionResponse(null, "videoPath 与 subtitlePath 不能为空");
        }
        Path videoPath = Paths.get(req.getVideoPath());
        Path subPath = Paths.get(req.getSubtitlePath());
        if (!Files.exists(videoPath)) {
            return new SubtitleFusionResponse(null, "视频文件不存在: " + videoPath);
        }
        if (!Files.exists(subPath)) {
            return new SubtitleFusionResponse(null, "字幕文件不存在: " + subPath);
        }
        if (!req.getSubtitlePath().toLowerCase().endsWith(".srt")) {
            return new SubtitleFusionResponse(null, "Java2D方案仅支持 .srt 字幕格式");
        }
        String out = fusionService.burnSrtViaJava2D(videoPath, subPath);
        return new SubtitleFusionResponse(out, "Java2D字幕渲染完成");
    }

    /**
     * Java2D 字幕渲染方案 - 混合版本，支持视频URL和字幕文件上传
     * 支持：SRT格式，自动编码处理，中文字幕优化
     */
    @PostMapping(value = "/burn-url-srt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public SubtitleFusionResponse burnUrlSrt(@RequestParam("videoUrl") String videoUrl, 
                                           @RequestParam("subtitleFile") MultipartFile subtitleFile) throws Exception {
        // 参数验证
        if (!StringUtils.hasText(videoUrl)) {
            return new SubtitleFusionResponse(null, "videoUrl 不能为空");
        }
        
        if (subtitleFile == null || subtitleFile.isEmpty()) {
            return new SubtitleFusionResponse(null, "字幕文件不能为空");
        }
        
        // 检查URL格式
        if (!isValidUrl(videoUrl)) {
            return new SubtitleFusionResponse(null, "无效的URL格式");
        }
        
        // 检查字幕文件扩展名
        String originalFilename = subtitleFile.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".srt")) {
            return new SubtitleFusionResponse(null, "Java2D方案仅支持 .srt 字幕格式");
        }
        
        // 保存上传的字幕文件到临时位置
        Path tempSubtitlePath = null;
        try {
            tempSubtitlePath = saveUploadedFile(subtitleFile);
            String minioUrl = fusionService.burnSrtViaJava2DFromVideoUrlAndFile(videoUrl, tempSubtitlePath);
            return new SubtitleFusionResponse(minioUrl, "Java2D字幕渲染完成，视频已上传到MinIO");
        } finally {
            // 清理临时字幕文件
            if (tempSubtitlePath != null && Files.exists(tempSubtitlePath)) {
                try {
                    Files.delete(tempSubtitlePath);
                    System.out.println("已清理临时字幕文件: " + tempSubtitlePath);
                } catch (Exception e) {
                    System.err.println("清理临时字幕文件失败: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * 简单的URL格式验证
     */
    private boolean isValidUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }

    /**
     * 保存上传的文件到临时目录
     */
    private Path saveUploadedFile(MultipartFile file) throws IOException {
        // 创建临时目录（如果不存在）
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "subtitle-fusion");
        if (!Files.exists(tempDir)) {
            Files.createDirectories(tempDir);
        }
        
        // 生成临时文件名
        String originalFilename = file.getOriginalFilename();
        String filename = System.currentTimeMillis() + "_" + (originalFilename != null ? originalFilename : "subtitle.srt");
        Path tempFile = tempDir.resolve(filename);
        
        // 保存文件
        Files.copy(file.getInputStream(), tempFile);
        System.out.println("已保存上传的字幕文件到: " + tempFile);
        
        return tempFile;
    }

    /**
     * 通过对象路径从MinIO下载文件（服务端代理）
     * 示例: /api/subtitles/download?path=videos/xxx.mp4
     */
    @GetMapping(value = "/download")
    public ResponseEntity<InputStreamResource> downloadFromMinio(@RequestParam("path") String objectPath) {
        if (!StringUtils.hasText(objectPath)) {
            return ResponseEntity.badRequest().build();
        }
        // 简单校验，防止路径穿越
        if (objectPath.startsWith("/") || objectPath.contains("..")) {
            return ResponseEntity.badRequest().build();
        }

        try {
            StatObjectResponse stat = minioService.statObject(objectPath);
            GetObjectResponse objectStream = minioService.getObject(objectPath);

            String contentType = stat.contentType();
            long contentLength = stat.size();
            String fileName = objectPath.substring(objectPath.lastIndexOf('/') + 1);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodeFileName(fileName) + "\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(contentLength)
                    .contentType(MediaType.parseMediaType(contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE))
                    .body(new InputStreamResource(objectStream));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private String encodeFileName(String fileName) {
        try {
            return java.net.URLEncoder.encode(fileName, java.nio.charset.StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");
        } catch (Exception e) {
            return fileName;
        }
    }

    // ========== 异步处理接口 ==========

    /**
     * 异步字幕渲染方案 - 提交任务，立即返回任务ID
     * 支持自定义任务ID，视频URL和字幕文件上传
     */
    @PostMapping(value = "/burn-url-srt/async", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public TaskResponse submitAsyncTask(@RequestParam("taskId") String taskId,
                                       @RequestParam("videoUrl") String videoUrl,
                                       @RequestParam("subtitleFile") MultipartFile subtitleFile) throws Exception {
        // 参数验证
        if (!StringUtils.hasText(taskId)) {
            return new TaskResponse(null, "taskId 不能为空");
        }

        if (!StringUtils.hasText(videoUrl)) {
            return new TaskResponse(taskId, "videoUrl 不能为空");
        }

        if (subtitleFile == null || subtitleFile.isEmpty()) {
            return new TaskResponse(taskId, "字幕文件不能为空");
        }

        // 检查任务ID是否已存在
        if (taskManagementService.taskExists(taskId)) {
            return new TaskResponse(taskId, "任务ID已存在，请使用不同的taskId");
        }

        // 检查URL格式
        if (!isValidUrl(videoUrl)) {
            return new TaskResponse(taskId, "无效的URL格式");
        }

        // 检查字幕文件扩展名
        String originalFilename = subtitleFile.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".srt")) {
            return new TaskResponse(taskId, "仅支持 .srt 字幕格式");
        }

        // 保存上传的字幕文件到临时位置
        Path tempSubtitlePath = null;
        try {
            tempSubtitlePath = saveUploadedFile(subtitleFile);

            // 创建任务
            TaskInfo taskInfo = taskManagementService.createTask(taskId);

            // 提交异步处理任务
            asyncFusionService.processVideoUrlWithSubtitleFileAsync(taskId, videoUrl, tempSubtitlePath);

            return new TaskResponse(taskInfo);

        } catch (Exception e) {
            // 如果创建任务失败，清理字幕文件
            if (tempSubtitlePath != null && Files.exists(tempSubtitlePath)) {
                try {
                    Files.delete(tempSubtitlePath);
                } catch (Exception cleanupException) {
                    System.err.println("清理字幕文件失败: " + cleanupException.getMessage());
                }
            }
            throw e;
        }
    }

    /**
     * 查询任务状态
     */
    @GetMapping(value = "/task/{taskId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TaskResponse getTaskStatus(@PathVariable String taskId) {
        TaskInfo taskInfo = taskManagementService.getTask(taskId);
        if (taskInfo == null) {
            return new TaskResponse(taskId, "任务不存在");
        }
        return new TaskResponse(taskInfo);
    }

    /**
     * 获取所有任务状态（可选，用于调试）
     */
    @GetMapping(value = "/tasks/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getTaskCount() {
        int count = taskManagementService.getTaskCount();
        return "{\"taskCount\": " + count + "}";
    }

    /**
     * 获取节点健康状态
     */
    @GetMapping(value = "/health/node", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getNodeHealth() {
        try {
            if (healthCheckService == null) {
                return "{\"status\": \"disabled\", \"message\": \"HealthCheckService is disabled (task.storage.type!=redis)\"}";
            }
            return healthCheckService.getNodeStatus();
        } catch (Exception e) {
            return "{\"status\": \"error\", \"message\": \"" + e.getMessage() + "\"}";
        }
    }

}

