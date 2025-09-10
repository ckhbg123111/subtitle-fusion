package com.zhongjia.subtitlefusion.controller;

import com.zhongjia.subtitlefusion.service.MinioService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/minio")
public class MinioUploadController {

    private final MinioService minioService;

    public MinioUploadController(MinioService minioService) {
        this.minioService = minioService;
    }

    /**
     * 上传文件到公开桶并返回可直接访问的URL
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("message", "文件不能为空");
            return err;
        }

        String originalFilename = file.getOriginalFilename();
        String url = minioService.uploadToPublicBucket(file.getInputStream(), file.getSize(), originalFilename != null ? originalFilename : "file.bin");

        Map<String, Object> resp = new HashMap<>();
        resp.put("url", url);
        return resp;
    }

    /**
     * 通过文件URL上传，验证URL有效性（格式、可访问、状态码、长度）
     */
    @PostMapping(value = "/upload-by-url", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> uploadByUrl(@RequestParam("fileUrl") String fileUrl) throws Exception {
        Map<String, Object> resp = new HashMap<>();

        if (fileUrl == null || fileUrl.isEmpty() || !(fileUrl.startsWith("http://") || fileUrl.startsWith("https://"))) {
            resp.put("message", "无效的URL（仅支持 http/https）");
            return resp;
        }

        URL urlObj = new URL(fileUrl);

        // 先用 HEAD（若服务不允许 HEAD 可能返回 403/405，此时回退 GET）
        long contentLength = -1L;
        int headCode;
        HttpURLConnection headConn = (HttpURLConnection) urlObj.openConnection();
        headConn.setRequestMethod("HEAD");
        headConn.setInstanceFollowRedirects(true);
        headConn.setConnectTimeout(8000);
        headConn.setReadTimeout(8000);
        headConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0 Safari/537.36");
        try {
            headCode = headConn.getResponseCode();
            if (headCode < 400) {
                contentLength = headConn.getContentLengthLong();
            }
        } finally {
            headConn.disconnect();
        }

        // 取文件名
        String path = urlObj.getPath();
        String name = path != null && path.lastIndexOf('/') >= 0 ? path.substring(path.lastIndexOf('/') + 1) : null;
        if (name == null || name.isEmpty()) {
            name = "file.bin";
        } else {
            name = URLDecoder.decode(name, StandardCharsets.UTF_8);
        }

        // 用 GET 拉取并上传（若 HEAD 未给长度，则在服务层走 unknown-size 分片上传）
        HttpURLConnection getConn = (HttpURLConnection) urlObj.openConnection();
        getConn.setRequestMethod("GET");
        getConn.setInstanceFollowRedirects(true);
        getConn.setConnectTimeout(10000);
        getConn.setReadTimeout(30000);
        getConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0 Safari/537.36");

        int getCode = getConn.getResponseCode();
        if (getCode >= 400) {
            resp.put("message", "URL不可访问，GET状态码=" + getCode + (headCode >= 400 ? (", HEAD状态码=" + headCode) : ""));
            return resp;
        }
        if (contentLength <= 0) {
            long len = getConn.getContentLengthLong();
            if (len > 0) contentLength = len;
        }

        try (var in = getConn.getInputStream()) {
            String url = minioService.uploadToPublicBucket(in, contentLength > 0 ? contentLength : -1L, name);
            resp.put("url", url);
            return resp;
        } finally {
            getConn.disconnect();
        }
    }
}


