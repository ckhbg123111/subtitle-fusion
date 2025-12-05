package com.zhongjia.subtitlefusion.service;

import com.zhongjia.subtitlefusion.ffmpeg.FFmpegExecutor;
import com.zhongjia.subtitlefusion.util.MediaIoUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AudioExtractionService {

	@Autowired
	private FFmpegExecutor ffmpeg;

	/**
	 * 使用 FFmpeg 从输入视频中提取音频。
	 * 优先尝试音频码流直拷到 M4A；失败时回退到 AAC 编码输出 M4A。
	 * @param inputVideo 输入视频路径
	 * @return 提取出的本地音频文件路径（.m4a）
	 */
	public Path extractAudio(Path inputVideo) throws Exception {
		if (inputVideo == null || !Files.exists(inputVideo)) {
			throw new IllegalArgumentException("输入视频不存在: " + inputVideo);
		}

		// 首选：音频直拷
		Path out = Files.createTempFile("extracted_audio_", ".m4a");
		List<String> copy = new ArrayList<>();
		copy.add("ffmpeg"); copy.add("-y");
		copy.add("-i"); copy.add(inputVideo.toAbsolutePath().toString());
		copy.add("-vn");
		copy.add("-c:a"); copy.add("copy");
		copy.add(out.toAbsolutePath().toString());
		try {
			ffmpeg.exec(copy.toArray(new String[0]), null);
			return out;
		} catch (RuntimeException ex) {
			// 兜底：AAC 编码输出
			MediaIoUtils.safeDelete(out);
			out = Files.createTempFile("extracted_audio_", ".m4a");
			List<String> trans = new ArrayList<>();
			trans.add("ffmpeg"); trans.add("-y");
			trans.add("-i"); trans.add(inputVideo.toAbsolutePath().toString());
			trans.add("-vn");
			trans.add("-c:a"); trans.add("aac");
			trans.add("-b:a"); trans.add("192k");
			trans.add("-ac"); trans.add("2");
			trans.add("-ar"); trans.add("44100");
			trans.add(out.toAbsolutePath().toString());
			ffmpeg.exec(trans.toArray(new String[0]), null);
			return out;
		}
	}
}


