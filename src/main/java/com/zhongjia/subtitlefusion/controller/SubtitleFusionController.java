package com.zhongjia.subtitlefusion.controller;

import com.zhongjia.subtitlefusion.model.SubtitleFusionLocalRequest;
import com.zhongjia.subtitlefusion.model.SubtitleFusionUrlRequest;
import com.zhongjia.subtitlefusion.model.SubtitleFusionResponse;
import com.zhongjia.subtitlefusion.service.SubtitleFusionService;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/subtitles")
public class SubtitleFusionController {

    private final SubtitleFusionService fusionService;

    public SubtitleFusionController(SubtitleFusionService fusionService) {
        this.fusionService = fusionService;
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
     * Java2D 字幕渲染方案 - URL版本，支持从网络下载视频和字幕文件
     * 支持：SRT格式，自动编码处理，中文字幕优化
     */
    @PostMapping(value = "/burn-url-srt", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public SubtitleFusionResponse burnUrlSrt(@RequestBody SubtitleFusionUrlRequest req) throws Exception {
        if (req == null || !StringUtils.hasText(req.getVideoUrl()) || !StringUtils.hasText(req.getSubtitleUrl())) {
            return new SubtitleFusionResponse(null, "videoUrl 与 subtitleUrl 不能为空");
        }
        
        // 检查URL格式
        if (!isValidUrl(req.getVideoUrl()) || !isValidUrl(req.getSubtitleUrl())) {
            return new SubtitleFusionResponse(null, "无效的URL格式");
        }
        
        // 检查字幕文件扩展名
        if (!req.getSubtitleUrl().toLowerCase().contains(".srt")) {
            return new SubtitleFusionResponse(null, "Java2D方案仅支持 .srt 字幕格式");
        }
        
        String out = fusionService.burnSrtViaJava2DFromUrls(req.getVideoUrl(), req.getSubtitleUrl());
        return new SubtitleFusionResponse(out, "Java2D字幕渲染完成（URL版本）");
    }
    
    /**
     * 简单的URL格式验证
     */
    private boolean isValidUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }


}

