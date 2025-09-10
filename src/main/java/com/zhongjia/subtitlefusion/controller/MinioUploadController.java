package com.zhongjia.subtitlefusion.controller;

import com.zhongjia.subtitlefusion.service.MinioService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
}


