package com.zhongjia.subtitlefusion.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 字幕融合主服务
 * 协调各个子服务完成字幕与视频的合成
 */
@Service
public class SubtitleFusionService {

    @Autowired
    private FileDownloadService downloadService;
    @Autowired
    private SubtitleParserService parserService;
    @Autowired
    private VideoProcessingService videoService;
    @Autowired
    private MinioService minioService;

    /**
     * 使用Java2D绘制字幕到视频帧上，支持SRT格式（本地文件版本）
     * 优点：稳定可靠，不依赖FFmpeg滤镜，完全控制字幕样式
     * 缺点：仅支持SRT格式，处理速度相对较慢
     */
    public String burnSrtViaJava2D(Path videoPath, Path srtPath) throws Exception {
        System.out.println("开始处理本地文件字幕合成...");
        System.out.println("视频文件: " + videoPath);
        System.out.println("字幕文件: " + srtPath);

        // 解析字幕文件
        List<SubtitleParserService.SrtCue> cues = parserService.parseSrtFile(srtPath);

        // 生成输出文件名
        String originalName = videoPath.getFileName().toString();
        String baseName = videoService.stripExt(originalName);

        // 处理视频并添加字幕
        return videoService.processVideoWithSubtitles(videoPath, cues, baseName);
    }

    /**
     * 使用Java2D绘制字幕到视频帧上，支持SRT格式（URL版本）
     * 从网络下载视频和字幕文件，然后进行合成，并上传到MinIO
     */
    public String burnSrtViaJava2DFromUrls(String videoUrl, String subtitleUrl) throws Exception {
        System.out.println("开始处理URL文件字幕合成...");
        System.out.println("视频URL: " + videoUrl);
        System.out.println("字幕URL: " + subtitleUrl);

        Path tempVideoPath = null;
        Path tempSubtitlePath = null;
        Path outputVideoPath = null;

        try {
            // 下载视频和字幕文件
            tempVideoPath = downloadService.downloadVideo(videoUrl);
            tempSubtitlePath = downloadService.downloadSubtitle(subtitleUrl);

            // 解析字幕文件
            List<SubtitleParserService.SrtCue> cues = parserService.parseSrtFile(tempSubtitlePath);

            // 生成输出文件名（从URL提取）
            String baseName = extractFileNameFromUrl(videoUrl);

            // 处理视频并添加字幕
            String outputPath = videoService.processVideoWithSubtitles(tempVideoPath, cues, baseName);
            outputVideoPath = Paths.get(outputPath);

            // 上传到MinIO并返回URL
            String fileName = outputVideoPath.getFileName().toString();
            String minioUrl = minioService.uploadFile(outputVideoPath, fileName);
            
            System.out.println("视频已上传到MinIO: " + minioUrl);
            return minioUrl;

        } finally {
            // 清理临时文件
            downloadService.cleanupTempFile(tempVideoPath);
            downloadService.cleanupTempFile(tempSubtitlePath);
            
            // 清理输出文件
            if (outputVideoPath != null && Files.exists(outputVideoPath)) {
                try {
                    Files.delete(outputVideoPath);
                    System.out.println("已清理本地输出文件: " + outputVideoPath);
                } catch (Exception e) {
                    System.err.println("清理本地输出文件失败: " + e.getMessage());
                }
            }
        }
    }

    /**
     * 使用Java2D绘制字幕到视频帧上，支持SRT格式（视频URL + 字幕文件混合版本）
     * 从网络下载视频，使用上传的字幕文件，然后进行合成，并上传到MinIO
     */
    public String burnSrtViaJava2DFromVideoUrlAndFile(String videoUrl, Path subtitlePath) throws Exception {
        System.out.println("开始处理视频URL+字幕文件合成...");
        System.out.println("视频URL: " + videoUrl);
        System.out.println("字幕文件: " + subtitlePath);

        Path tempVideoPath = null;
        Path outputVideoPath = null;

        try {
            // 下载视频文件
            tempVideoPath = downloadService.downloadVideo(videoUrl);

            // 解析字幕文件
            List<SubtitleParserService.SrtCue> cues = parserService.parseSrtFile(subtitlePath);

            // 生成输出文件名（从URL提取）
            String baseName = extractFileNameFromUrl(videoUrl);

            // 处理视频并添加字幕
            String outputPath = videoService.processVideoWithSubtitles(tempVideoPath, cues, baseName);
            outputVideoPath = Paths.get(outputPath);

            // 上传到MinIO并返回URL
            String fileName = outputVideoPath.getFileName().toString();
            String minioUrl = minioService.uploadFile(outputVideoPath, fileName);
            
            System.out.println("视频已上传到MinIO: " + minioUrl);
            return minioUrl;

        } finally {
            // 清理临时文件
            downloadService.cleanupTempFile(tempVideoPath);
            
            // 清理输出文件
            if (outputVideoPath != null && Files.exists(outputVideoPath)) {
                try {
                    Files.delete(outputVideoPath);
                    System.out.println("已清理本地输出文件: " + outputVideoPath);
                } catch (Exception e) {
                    System.err.println("清理本地输出文件失败: " + e.getMessage());
                }
            }
        }
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


}
