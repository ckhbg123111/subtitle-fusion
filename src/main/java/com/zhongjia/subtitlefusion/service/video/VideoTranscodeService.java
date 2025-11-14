package com.zhongjia.subtitlefusion.service.video;

import com.zhongjia.subtitlefusion.ffmpeg.FFmpegExecutor;
import com.zhongjia.subtitlefusion.util.MediaProbeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 在上传到对象存储前，对 HEVC/H.265 编码的视频做 H.264 转码；音频优先直拷，必要时回落 AAC。
 */
@Service
@Slf4j
public class VideoTranscodeService {

    @Autowired
    private FFmpegExecutor ffmpegExecutor;

    /**
     * 若输入为 HEVC/H.265，则转码为 H.264（libx264），音频直拷；否则返回原文件。
     * 返回可直接上传的文件路径。
     */
    public Path transcodeIfNeeded(Path input) throws Exception {
        if (input == null || !Files.exists(input)) {
            throw new IllegalArgumentException("Input video does not exist: " + input);
        }
        String codec = "";
        try {
            codec = MediaProbeUtils.probeVideoCodecName(input);
        } catch (Exception e) {
            // 探测失败不直接终止，默认尝试转码以提升兼容性
            if (log.isWarnEnabled()) log.warn("Failed to probe codec for {}: {}", input, e.getMessage());
        }
        if (!needsTranscode(codec)) {
            return input;
        }

        Path output = buildOutputPath(input);
        // 首选：音频直拷
        List<String> cmd = new ArrayList<>();
        cmd.add("ffmpeg"); cmd.add("-y");
        cmd.add("-i"); cmd.add(input.toAbsolutePath().toString());
        cmd.add("-map"); cmd.add("0:v:0");
        cmd.add("-map"); cmd.add("0:a?");
        cmd.add("-c:v"); cmd.add("libx264");
        cmd.add("-crf"); cmd.add("23");
        cmd.add("-preset"); cmd.add("medium");
        cmd.add("-pix_fmt"); cmd.add("yuv420p");
        cmd.add("-c:a"); cmd.add("copy");
        cmd.add("-movflags"); cmd.add("+faststart");
        cmd.add(output.toAbsolutePath().toString());

        try {
            ffmpegExecutor.exec(cmd.toArray(new String[0]), null);
            return output;
        } catch (RuntimeException ex) {
            // 兜底：音频编码为 AAC，避免复用失败
            if (log.isWarnEnabled()) log.warn("Audio copy failed, fallback to AAC. {}", ex.getMessage());
            // 清理可能的半成品
            safeDelete(output);

            List<String> fallback = new ArrayList<>();
            fallback.add("ffmpeg"); fallback.add("-y");
            fallback.add("-i"); fallback.add(input.toAbsolutePath().toString());
            fallback.add("-map"); fallback.add("0:v:0");
            fallback.add("-map"); fallback.add("0:a?");
            fallback.add("-c:v"); fallback.add("libx264");
            fallback.add("-crf"); fallback.add("23");
            fallback.add("-preset"); fallback.add("medium");
            fallback.add("-pix_fmt"); fallback.add("yuv420p");
            fallback.add("-c:a"); fallback.add("aac");
            fallback.add("-b:a"); fallback.add("128k");
            fallback.add("-ac"); fallback.add("2");
            fallback.add("-ar"); fallback.add("48000");
            fallback.add("-movflags"); fallback.add("+faststart");
            fallback.add(output.toAbsolutePath().toString());

            ffmpegExecutor.exec(fallback.toArray(new String[0]), null);
            return output;
        }
    }

    private boolean needsTranscode(String codecLower) {
        if (codecLower == null || codecLower.isEmpty()) return true; // 探测失败，为稳妥起见转码
        return "hevc".equalsIgnoreCase(codecLower) || "h265".equalsIgnoreCase(codecLower);
    }

    private Path buildOutputPath(Path input) {
        String name = input.getFileName().toString();
        int dot = name.lastIndexOf('.');
        String base = (dot > 0 ? name.substring(0, dot) : name);
        String outName = base + ".transcoded.mp4";
        return input.getParent() != null ? input.getParent().resolve(outName) : Path.of(outName);
    }

    private void safeDelete(Path p) {
        try {
            if (p != null && Files.exists(p)) Files.delete(p);
        } catch (Exception ignore) {}
    }
}


