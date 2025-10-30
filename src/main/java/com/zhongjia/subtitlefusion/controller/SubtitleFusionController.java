package com.zhongjia.subtitlefusion.controller;

import com.zhongjia.subtitlefusion.model.LineCapacityResponse;
import com.zhongjia.subtitlefusion.model.SubtitleFusionV2Request;
import com.zhongjia.subtitlefusion.model.TaskInfo;
import com.zhongjia.subtitlefusion.model.TaskResponse;
import com.zhongjia.subtitlefusion.service.*;
import io.minio.GetObjectResponse;
import io.minio.StatObjectResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/subtitles")
@Slf4j
public class SubtitleFusionController {

    @Autowired
    private AsyncSubtitleFusionService asyncFusionService;
    @Autowired
    private DistributedTaskManagementService taskManagementService;
    @Autowired
    private MinioService minioService;
    @Autowired(required = false)
    private HealthCheckService healthCheckService;
    @Autowired
    private SubtitleMetricsService subtitleMetricsService;
    @Autowired
    private AssSubtitleAsyncService assSubtitleAsyncService;

    /**
     * 计算字幕每行建议最大字数（估算）
     * 示例: /api/subtitles/line-capacity?width=1920&height=1080
     */
    @GetMapping(value = "/line-capacity", produces = MediaType.APPLICATION_JSON_VALUE)
    public LineCapacityResponse getLineCapacity(@RequestParam("width") int width,
                                                @RequestParam("height") int height,
                                                @RequestParam(value = "fontFamily", required = false) String fontFamily,
                                                @RequestParam(value = "fontStyle", required = false) String fontStyle,
                                                @RequestParam(value = "fontSizePx", required = false) Integer fontSizePx,
                                                @RequestParam(value = "fontScale", required = false) Float fontScale,
                                                @RequestParam(value = "minFontSizePx", required = false) Integer minFontSizePx,
                                                @RequestParam(value = "marginHPercent", required = false) Float marginHPercent,
                                                @RequestParam(value = "marginHpx", required = false) Integer marginHpx,
                                                @RequestParam(value = "cjkChar", required = false) Character cjkChar,
                                                @RequestParam(value = "englishSample", required = false) String englishSample,
                                                @RequestParam(value = "strategy", required = false) String strategy) {
        SubtitleMetricsService.Options opt = new SubtitleMetricsService.Options();
        opt.fontFamily = fontFamily;
        opt.fontStyle = fontStyle;
        opt.fontSizePx = fontSizePx;
        opt.fontScale = fontScale;
        opt.minFontSizePx = minFontSizePx;
        opt.marginHPercent = marginHPercent;
        opt.marginHpx = marginHpx;
        opt.cjkChar = cjkChar;
        opt.englishSample = englishSample;
        opt.strategy = strategy;

        SubtitleMetricsService.LineCapacity cap = subtitleMetricsService.calculateLineCapacityWithOptions(width, height, opt);

        // 计算用于回显的实际字号与边距像素（与服务内一致的解析方式）
        int resolvedMarginH;
        if (marginHpx != null && marginHpx >= 0) {
            resolvedMarginH = marginHpx;
        } else if (marginHPercent != null && marginHPercent >= 0) {
            resolvedMarginH = Math.round(width * (marginHPercent / 100f));
        } else {
            resolvedMarginH = Math.max(12, Math.round(width * 0.06f));
        }

        int baseFont = Math.max(18, Math.round(height * 0.035f));
        int resolvedFontSize;
        if (fontSizePx != null && fontSizePx > 0) {
            resolvedFontSize = fontSizePx;
        } else if (fontScale != null && fontScale > 0) {
            resolvedFontSize = Math.round(baseFont * fontScale);
        } else {
            resolvedFontSize = baseFont;
        }
        if (minFontSizePx != null && minFontSizePx > 0) {
            resolvedFontSize = Math.max(resolvedFontSize, minFontSizePx);
        }

        String resolvedFamily = (fontFamily != null && !fontFamily.isEmpty()) ? fontFamily : "Microsoft YaHei";
        String resolvedStyle = fontStyle != null ? fontStyle : "plain";
        String resolvedStrategy = strategy != null ? strategy : "default";

        return new LineCapacityResponse(width, height, cap.maxCharsChinese, cap.maxCharsEnglish, cap.conservative)
                .withOptionsEcho(resolvedFamily, resolvedStyle, resolvedFontSize, resolvedMarginH, resolvedStrategy);
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
        log.info("已保存上传的字幕文件到: {}", tempFile);

        return tempFile;
    }

    /**
     * 通过对象路径从MinIO下载文件（服务端代理）
     * 示例: /api/subtitles/download?path=videos/xxx.mp4
     */
    @GetMapping(value = "/download")
    public ResponseEntity<InputStreamResource> downloadFromMinio(@RequestParam("path") String objectPath,
                                                                 @RequestHeader(value = "Range", required = false) String rangeHeader) {
        if (!StringUtils.hasText(objectPath)) {
            return ResponseEntity.badRequest().build();
        }
        // 简单校验，防止路径穿越
        if (objectPath.startsWith("/") || objectPath.contains("..")) {
            return ResponseEntity.badRequest().build();
        }

        try {
            StatObjectResponse stat = minioService.statObject(objectPath);
            String contentType = stat.contentType();
            long totalLength = stat.size();
            String fileName = objectPath.substring(objectPath.lastIndexOf('/') + 1);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.ACCEPT_RANGES, "bytes");

            // 处理 Range 请求，支持视频按需加载
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                String rangeValue = rangeHeader.replace("bytes=", "").trim();
                // 暂不支持多段范围
                if (rangeValue.contains(",")) {
                    HttpHeaders h = new HttpHeaders();
                    h.add(HttpHeaders.CONTENT_RANGE, "bytes */" + totalLength);
                    return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE).headers(h).build();
                }

                long start;
                long end;
                try {
                    if (rangeValue.startsWith("-")) {
                        // 后缀长度：例如 bytes=-500
                        long suffixLength = Long.parseLong(rangeValue.substring(1));
                        if (suffixLength <= 0) throw new IllegalArgumentException("invalid suffix");
                        start = Math.max(totalLength - suffixLength, 0);
                        end = totalLength - 1;
                    } else {
                        String[] parts = rangeValue.split("-");
                        start = Long.parseLong(parts[0]);
                        if (parts.length > 1 && parts[1] != null && !parts[1].isEmpty()) {
                            end = Long.parseLong(parts[1]);
                        } else {
                            end = totalLength - 1;
                        }
                    }
                } catch (Exception parseEx) {
                    HttpHeaders h = new HttpHeaders();
                    h.add(HttpHeaders.CONTENT_RANGE, "bytes */" + totalLength);
                    return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE).headers(h).build();
                }

                if (start < 0 || end < start || start >= totalLength) {
                    HttpHeaders h = new HttpHeaders();
                    h.add(HttpHeaders.CONTENT_RANGE, "bytes */" + totalLength);
                    return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE).headers(h).build();
                }

                long contentLength = end - start + 1;
                headers.add(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + totalLength);
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + encodeFileName(fileName) + "\"");

                GetObjectResponse rangeStream = minioService.getObjectRange(objectPath, start, contentLength);
                MediaType mt = MediaType.parseMediaType(contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE);
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .headers(headers)
                        .contentLength(contentLength)
                        .contentType(mt)
                        .body(new InputStreamResource(rangeStream));
            }

            // 非 Range 请求：整文件返回，但使用 inline 以便浏览器可直接播放
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + encodeFileName(fileName) + "\"");
            GetObjectResponse objectStream = minioService.getObject(objectPath);
            MediaType mt = MediaType.parseMediaType(contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE);
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(totalLength)
                    .contentType(mt)
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

    @PostMapping(value = "/burn-as-ass/async", produces = MediaType.APPLICATION_JSON_VALUE)
    public TaskResponse submitBurnAsAss(@RequestBody SubtitleFusionV2Request subtitleFusionV2Request) {
        String taskId = subtitleFusionV2Request.getTaskId();
        String videoUrl = subtitleFusionV2Request.getVideoUrl();
        if (!StringUtils.hasText(taskId)) {
            return new TaskResponse(null, "taskId 不能为空");
        }
        if (!StringUtils.hasText(videoUrl) || !isValidUrl(videoUrl)) {
            return new TaskResponse(taskId, "无效的videoUrl");
        }
        if (subtitleFusionV2Request.getSubtitleInfo() == null ||
                subtitleFusionV2Request.getSubtitleInfo().getCommonSubtitleInfoList() == null ||
                subtitleFusionV2Request.getSubtitleInfo().getCommonSubtitleInfoList().isEmpty()) {
            return new TaskResponse(taskId, "subtitleInfo.commonSubtitleInfoList 不能为空");
        }

        if (taskManagementService.taskExists(taskId)) {
            return new TaskResponse(taskId, "任务ID已存在，请使用不同的taskId");
        }

        try {
            TaskInfo taskInfo = taskManagementService.createTask(taskId);
            assSubtitleAsyncService.processAsync(taskId, subtitleFusionV2Request);
            return new TaskResponse(taskInfo);
        } catch (Exception e) {
            return new TaskResponse(taskId, e.getMessage());
        }
    }

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
                    log.warn("清理字幕文件失败: {}", cleanupException.getMessage());
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

