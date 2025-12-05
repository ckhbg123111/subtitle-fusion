package com.zhongjia.subtitlefusion.controller;

import com.zhongjia.subtitlefusion.model.UploadResult;
import com.zhongjia.subtitlefusion.service.AudioExtractionService;
import com.zhongjia.subtitlefusion.service.FileDownloadService;
import com.zhongjia.subtitlefusion.service.MinioService;
import com.zhongjia.subtitlefusion.util.MediaIoUtils;
import com.zhongjia.subtitlefusion.util.MediaProbeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/audio")
public class AudioController {

	@Autowired
	private FileDownloadService fileDownloadService;
	@Autowired
	private AudioExtractionService audioExtractionService;
	@Autowired
	private MinioService minioService;

	/**
	 * 提供视频 URL，提取音频，上传至 MinIO（公开桶优先），返回 {url, path}
	 */
	@PostMapping(value = "/extract-upload-by-url", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Object> extractAndUploadByUrl(@RequestParam("videoUrl") String videoUrl) throws Exception {
		Map<String, Object> resp = new HashMap<>();
		if (!isValidUrl(videoUrl)) {
			resp.put("message", "无效的URL（仅支持 http/https）");
			return resp;
		}

		Path downloaded = null;
		Path audio = null;
		try {
			downloaded = fileDownloadService.downloadVideo(videoUrl);

			// 没有音频流直接返回提示
			boolean hasAudio = false;
			try {
				hasAudio = MediaProbeUtils.hasAudioStream(downloaded);
			} catch (Exception ignore) {}
			if (!hasAudio) {
				resp.put("message", "视频不包含音频流");
				return resp;
			}

			audio = audioExtractionService.extractAudio(downloaded);

			// 构造音频文件名（沿用视频名，扩展名改为 .m4a）
			String name = guessFileName(videoUrl, "audio.m4a");
			int dot = name.lastIndexOf('.');
			if (dot > 0) name = name.substring(0, dot) + ".m4a";

			long size = Files.size(audio);
			try (InputStream in = Files.newInputStream(audio)) {
				UploadResult result = minioService.uploadToPublicBucket(in, size, name);
				resp.put("url", result.getUrl());
				resp.put("path", result.getPath());
				return resp;
			}
		} finally {
			MediaIoUtils.safeDelete(audio);
			MediaIoUtils.safeDelete(downloaded);
		}
	}

	private boolean isValidUrl(String url) {
		return url != null && (url.startsWith("http://") || url.startsWith("https://"));
	}

	private String guessFileName(String fileUrl, String def) {
		try {
			URL u = new URL(fileUrl);
			String p = u.getPath();
			if (p != null && !p.isEmpty()) {
				int i = p.lastIndexOf('/');
				String n = i >= 0 ? p.substring(i + 1) : p;
				if (!n.isEmpty()) return java.net.URLDecoder.decode(n, java.nio.charset.StandardCharsets.UTF_8);
			}
		} catch (Exception ignored) {}
		return def;
	}
}


