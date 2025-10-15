package com.zhongjia.subtitlefusion.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 文件下载服务
 * 负责从URL下载视频和字幕文件到本地临时目录
 */
@Service
public class FileDownloadService {

    private static final int CONNECT_TIMEOUT = 30000; // 30秒连接超时
    private static final int READ_TIMEOUT = 60000; // 60秒读取超时
    
    /**
     * 从URL下载文件到临时目录
     * @param fileUrl 文件URL
     * @param fileExtension 文件扩展名（包括点，如".mp4"）
     * @return 下载后的本地文件路径
     */
    public Path downloadFile(String fileUrl, String fileExtension) throws IOException {
        System.out.println("开始下载文件: " + fileUrl);
        
        // 创建临时文件
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String tempFileName = "download_" + timestamp + "_" + System.currentTimeMillis() + fileExtension;
        Path tempFile = Paths.get(System.getProperty("java.io.tmpdir"), tempFileName);
        
        // 建立连接并下载（通过 URI 规范化编码路径/查询，兼容中文等特殊字符）
        URL rawUrl = new URL(fileUrl);
        URI normalizedUri;
        try {
            normalizedUri = new URI(
                    rawUrl.getProtocol(),
                    rawUrl.getUserInfo(),
                    rawUrl.getHost(),
                    rawUrl.getPort(),
                    rawUrl.getPath(),
                    rawUrl.getQuery(),
                    null
            );
        } catch (Exception e) {
            throw new IOException("规范化URL失败: " + e.getMessage(), e);
        }
        HttpURLConnection connection = (HttpURLConnection) normalizedUri.toURL().openConnection();
        connection.setInstanceFollowRedirects(true);
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        connection.setRequestProperty("User-Agent", "SubtitleFusion/1.0");
        
        try {
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("下载失败，HTTP响应码: " + responseCode + ", URL: " + fileUrl);
            }
            
            try (InputStream inputStream = connection.getInputStream()) {
                Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }
            
            System.out.println("文件下载完成: " + tempFile);
            return tempFile;
            
        } finally {
            connection.disconnect();
        }
    }
    
    /**
     * 从URL下载视频文件
     */
    public Path downloadVideo(String videoUrl) throws IOException {
        String extension = extractExtension(videoUrl, ".mp4");
        return downloadFile(videoUrl, extension);
    }
    
    /**
     * 从URL下载字幕文件
     */
    public Path downloadSubtitle(String subtitleUrl) throws IOException {
        String extension = extractExtension(subtitleUrl, ".srt");
        return downloadFile(subtitleUrl, extension);
    }
    
    /**
     * 从URL中提取文件扩展名
     */
    private String extractExtension(String url, String defaultExtension) {
        try {
            // 移除查询参数
            String path = url.split("\\?")[0];
            int lastDot = path.lastIndexOf('.');
            if (lastDot > 0 && lastDot < path.length() - 1) {
                String ext = path.substring(lastDot).toLowerCase();
                // 验证是否为合理的扩展名（最多5个字符）
                if (ext.length() <= 5 && ext.matches("\\.[a-z0-9]+")) {
                    return ext;
                }
            }
        } catch (Exception e) {
            System.err.println("提取文件扩展名失败: " + e.getMessage());
        }
        return defaultExtension;
    }
    
    /**
     * 清理临时文件
     */
    public void cleanupTempFile(Path tempFile) {
        if (tempFile != null) {
            try {
                Files.deleteIfExists(tempFile);
                System.out.println("已清理临时文件: " + tempFile);
            } catch (Exception e) {
                System.err.println("清理临时文件失败: " + tempFile + ", 错误: " + e.getMessage());
            }
        }
    }
}
