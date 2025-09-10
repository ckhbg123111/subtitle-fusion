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

        // 先用 HEAD 校验可达性与长度
        HttpURLConnection headConn = (HttpURLConnection) urlObj.openConnection();
        headConn.setRequestMethod("HEAD");
        headConn.setInstanceFollowRedirects(true);
        headConn.setConnectTimeout(8000);
        headConn.setReadTimeout(8000);
        int code = headConn.getResponseCode();
        if (code >= 400) {
            resp.put("message", "URL不可访问，状态码=" + code);
            return resp;
        }
        long contentLength = headConn.getContentLengthLong();

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

        try (var in = getConn.getInputStream()) {
            String url = minioService.uploadToPublicBucket(in, contentLength, name);
            resp.put("url", url);
            return resp;
        }
    }
}


