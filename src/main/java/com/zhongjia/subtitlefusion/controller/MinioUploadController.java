package com.zhongjia.subtitlefusion.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zhongjia.subtitlefusion.model.Result;
import com.zhongjia.subtitlefusion.service.MinioService;
import com.zhongjia.subtitlefusion.model.UploadResult;
import com.zhongjia.subtitlefusion.service.TemporaryCloudRenderService;
import com.zhongjia.subtitlefusion.service.video.VideoTranscodeService;
import com.zhongjia.subtitlefusion.util.MediaProbeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/minio")
@Slf4j
public class MinioUploadController {

    @Autowired
    private MinioService minioService;
    @Autowired
    private VideoTranscodeService transcodeService;
    @Autowired
    private TemporaryCloudRenderService temporaryCloudRenderService;

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
        UploadResult result = minioService.uploadToPublicBucket(file.getInputStream(), file.getSize(), originalFilename != null ? originalFilename : "file.bin");

        Map<String, Object> resp = new HashMap<>();
        resp.put("url", result.getUrl());
        resp.put("path", result.getPath());
        return resp;
    }

    /**
     * 上传文件到公开桶并返回可直接访问的URL
     */
    @PostMapping(value = "/upload-v2", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Result<Map<String, Object>> uploadV2(@RequestParam("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        UploadResult result = minioService.uploadToPublicBucket(file.getInputStream(), file.getSize(), originalFilename != null ? originalFilename : "file.bin");

        Map<String, Object> resp = new HashMap<>();
        resp.put("url", result.getUrl());
        resp.put("path", result.getPath());
        return Result.success(resp);
    }

    /**
     * 转存云渲染结果：下载 cloudurl 指向的成片并上传到 MinIO，返回 UploadResult（包含 url/path）。
     * 入参名固定为 cloudurl。
     */
    @PostMapping(value = "/transfer-cloud-render", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Result<UploadResult> transferCloudRenderResult(@RequestBody TransferCloudRenderRequest req) throws Exception {
        String cloudurl = req != null ? req.getCloudurl() : null;
        if (cloudurl == null || cloudurl.isEmpty() || !(cloudurl.startsWith("http://") || cloudurl.startsWith("https://"))) {
            return Result.error("无效的cloudurl（仅支持 http/https）");
        }
        return Result.success(temporaryCloudRenderService.transferCloudRenderResultToMinio(cloudurl));
    }

    public static class TransferCloudRenderRequest {
        @JsonProperty("cloudurl")
        private String cloudurl;

        public String getCloudurl() {
            return cloudurl;
        }

        public void setCloudurl(String cloudurl) {
            this.cloudurl = cloudurl;
        }
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
            UploadResult result = minioService.uploadToPublicBucket(in, contentLength > 0 ? contentLength : -1L, name);
            resp.put("url", result.getUrl());
            resp.put("path", result.getPath());
            return resp;
        } finally {
            getConn.disconnect();
        }
    }

    /**
     * 通过文件URL上传到默认（非公开）桶，返回对象在桶内的路径
     */
    @PostMapping(value = "/upload-by-url-path", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> uploadByUrlReturnPath(@RequestParam("fileUrl") String fileUrl) throws Exception {
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

        // 用 GET 拉取到临时文件，再上传到默认桶（返回对象路径）
        HttpURLConnection getConn = (HttpURLConnection) urlObj.openConnection();
        getConn.setRequestMethod("GET");
        getConn.setInstanceFollowRedirects(true);
        getConn.setConnectTimeout(10000);
        getConn.setReadTimeout(30000);
        getConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0 Safari/537.36");

        Path temp = null;
        try {
            int getCode = getConn.getResponseCode();
            if (getCode >= 400) {
                resp.put("message", "URL不可访问，GET状态码=" + getCode + (headCode >= 400 ? (", HEAD状态码=" + headCode) : ""));
                return resp;
            }
            if (contentLength <= 0) {
                long len = getConn.getContentLengthLong();
                if (len > 0) contentLength = len;
            }

            temp = Files.createTempFile("upload_by_url_", ".tmp");
            try (var in = getConn.getInputStream(); var out = Files.newOutputStream(temp)) {
                byte[] buf = new byte[8192];
                int r;
                while ((r = in.read(buf)) != -1) {
                    out.write(buf, 0, r);
                }
            }

            String objectPath = minioService.uploadFile(temp, name);
            resp.put("path", objectPath);
            return resp;
        } finally {
            getConn.disconnect();
            if (temp != null) {
                try { Files.deleteIfExists(temp); } catch (Exception ignore) {}
            }
        }
    }

    /**
     * 通过视频URL上传到默认桶并返回对象路径；若为 HEVC/H.265，则转码为 H.264（音频直拷）后再上传。
     */
    @PostMapping(value = "/upload-video-by-url-path", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> uploadVideoByUrlReturnPath(@RequestParam("fileUrl") String fileUrl) throws Exception {
        Map<String, Object> resp = new HashMap<>();

        if (fileUrl == null || fileUrl.isEmpty() || !(fileUrl.startsWith("http://") || fileUrl.startsWith("https://"))) {
            resp.put("message", "无效的URL（仅支持 http/https）");
            return resp;
        }

        URL urlObj = new URL(fileUrl);

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

        String path = urlObj.getPath();
        String name = path != null && path.lastIndexOf('/') >= 0 ? path.substring(path.lastIndexOf('/') + 1) : null;
        if (name == null || name.isEmpty()) {
            name = "video.bin";
        } else {
            name = URLDecoder.decode(name, StandardCharsets.UTF_8);
        }

        HttpURLConnection getConn = (HttpURLConnection) urlObj.openConnection();
        getConn.setRequestMethod("GET");
        getConn.setInstanceFollowRedirects(true);
        getConn.setConnectTimeout(10000);
        getConn.setReadTimeout(30000);
        getConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0 Safari/537.36");

        Path temp = null;
        Path toUpload = null;
        try {
            int getCode = getConn.getResponseCode();
            if (getCode >= 400) {
                resp.put("message", "URL不可访问，GET状态码=" + getCode + (headCode >= 400 ? (", HEAD状态码=" + headCode) : ""));
                return resp;
            }
            if (contentLength <= 0) {
                long len = getConn.getContentLengthLong();
                if (len > 0) contentLength = len;
            }

            temp = Files.createTempFile("upload_video_by_url_", ".tmp");
            try (var in = getConn.getInputStream(); var out = Files.newOutputStream(temp)) {
                byte[] buf = new byte[8192];
                int r;
                while ((r = in.read(buf)) != -1) {
                    out.write(buf, 0, r);
                }
            }

            // 探测并按需转码（仅 HEVC/H.265 才转）
            String codec = "";
            try {
                codec = MediaProbeUtils.probeVideoCodecName(temp);
            } catch (Exception ignore) {}

            if ("hevc".equalsIgnoreCase(codec) || "h265".equalsIgnoreCase(codec)) {
                Path transcoded = transcodeService.transcodeIfNeeded(temp);
                toUpload = transcoded;
                // 转码后统一扩展名为 .mp4
                String base = name;
                int dot = base.lastIndexOf('.');
                if (dot > 0) base = base.substring(0, dot);
                name = base + ".mp4";
            } else {
                toUpload = temp;
            }

            String objectPath = minioService.uploadFile(toUpload, name);
            resp.put("path", objectPath);
            return resp;
        } finally {
            getConn.disconnect();
            if (toUpload != null && temp != null && !toUpload.equals(temp)) {
                try { Files.deleteIfExists(toUpload); } catch (Exception ignore) {}
            }
            if (temp != null) {
                try { Files.deleteIfExists(temp); } catch (Exception ignore) {}
            }
        }
    }

    /**
     *
     * @param file 上传视频文件
     * 探测视频首帧
     * 上传视频及首帧到minIO
     * @return 视频文件url，path  首帧图片url，path
     */
    @PostMapping(value = "/upload-video-get-first-frame", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Result<Map<String, Object>> uploadVideoAndGetFirstFrame(@RequestParam("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        Path tempVideo = null;
        Path firstFrame = null;
        try {
            // 规范化原始文件名（避免带路径）
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.trim().isEmpty()) {
                originalFilename = "video.mp4";
            } else {
                originalFilename = originalFilename.replace("\\", "/");
                int slash = originalFilename.lastIndexOf('/');
                if (slash >= 0) {
                    originalFilename = originalFilename.substring(slash + 1);
                }
                if (originalFilename.trim().isEmpty()) {
                    originalFilename = "video.mp4";
                }
            }

            // 临时视频文件后缀尽量沿用扩展名，便于 ffmpeg/工具识别（不依赖但更稳）
            String suffix = ".mp4";
            int dot = originalFilename.lastIndexOf('.');
            if (dot > 0 && dot < originalFilename.length() - 1) {
                String ext = originalFilename.substring(dot);
                // 简单白名单：.xxx (1~10位字母数字)
                if (ext.matches("\\.[A-Za-z0-9]{1,10}")) {
                    suffix = ext.toLowerCase();
                }
            }

            tempVideo = Files.createTempFile("upload_video_", suffix);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, tempVideo, StandardCopyOption.REPLACE_EXISTING);
            }

            // 生成首帧文件名（用于上传命名），本地落盘用随机临时文件避免冲突
            String base = originalFilename.replaceAll("\\.[A-Za-z0-9]+$", "");
            if (base.trim().isEmpty()) base = "video";
            String frameFileName = base + "_first_frame.jpg";
            firstFrame = Files.createTempFile("first_frame_", ".jpg");

            boolean ok = MediaProbeUtils.extractFirstFrame(tempVideo, firstFrame);
            if (!ok || !Files.exists(firstFrame)) {
                return Result.error("无法从视频中抽取第一帧");
            }

            // 上传视频与首帧到公开桶（不转码）
            UploadResult videoUp;
            try (InputStream in = Files.newInputStream(tempVideo)) {
                long size = Files.size(tempVideo);
                videoUp = minioService.uploadToPublicBucket(in, size, originalFilename);
            }

            UploadResult frameUp;
            try (InputStream in = Files.newInputStream(firstFrame)) {
                long size = Files.size(firstFrame);
                frameUp = minioService.uploadToPublicBucket(in, size, frameFileName);
            }

            Map<String, Object> resp = new HashMap<>();
            resp.put("videoUrl", videoUp.getUrl());
            resp.put("videoPath", videoUp.getPath());
            resp.put("firstFrameUrl", frameUp.getUrl());
            resp.put("firstFramePath", frameUp.getPath());
            return Result.success(resp);
        } catch (Exception e) {
            log.error("上传视频并抽取首帧失败", e);
            return Result.error("上传失败: " + e.getMessage());
        } finally {
            if (firstFrame != null) {
                try { Files.deleteIfExists(firstFrame); } catch (Exception ignore) {}
            }
            if (tempVideo != null) {
                try { Files.deleteIfExists(tempVideo); } catch (Exception ignore) {}
            }
        }
    }
}


