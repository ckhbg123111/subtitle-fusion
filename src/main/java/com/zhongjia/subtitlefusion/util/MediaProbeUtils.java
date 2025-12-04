package com.zhongjia.subtitlefusion.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * 调用 ffprobe / ffmpeg 获取媒体信息和抽帧的简易工具。
 */
public final class MediaProbeUtils {

    private MediaProbeUtils() {}

    /**
     * 获取媒体总时长（秒）。失败返回 0。
     */
    public static double probeDurationSeconds(java.nio.file.Path media) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                "ffprobe", "-v", "error",
                "-show_entries", "format=duration",
                "-of", "default=nk=1:nw=1",
                media.toAbsolutePath().toString()
        );
        Process p = pb.start();
        String line = null;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            line = r.readLine();
        }
        p.waitFor();
        if (line == null || line.isEmpty()) return 0d;
        try {
            return Math.max(0d, Double.parseDouble(line.trim()));
        } catch (Exception ignore) {
            return 0d;
        }
    }

    /**
     * 是否包含音频流。
     */
    public static boolean hasAudioStream(java.nio.file.Path media) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                "ffprobe", "-v", "error",
                "-select_streams", "a:0",
                "-show_entries", "stream=index",
                "-of", "csv=p=0",
                media.toAbsolutePath().toString()
        );
        Process p = pb.start();
        String line = null;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            line = r.readLine();
        }
        p.waitFor();
        return line != null && !line.trim().isEmpty();
    }

    /**
     * 探测视频分辨率，返回 [width, height]；失败返回 {1920,1080}。
     */
    public static int[] probeVideoResolution(java.nio.file.Path media) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                "ffprobe", "-v", "error",
                "-select_streams", "v:0",
                "-show_entries", "stream=width,height",
                "-of", "csv=s=x:p=0",
                media.toAbsolutePath().toString()
        );
        Process p = pb.start();
        String line = null;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            line = r.readLine();
        }
        p.waitFor();
        if (line != null && !line.trim().isEmpty()) {
            String s = line.trim(); // e.g. 1920x1080
            int x = s.indexOf('x');
            if (x > 0) {
                try {
                    int w = Integer.parseInt(s.substring(0, x));
                    int h = Integer.parseInt(s.substring(x + 1));
                    if (w > 0 && h > 0) return new int[]{w, h};
                } catch (Exception ignore) {}
            }
        }
        return new int[]{1920, 1080};
    }

    /**
     * 探测视频编码名称（如 h264、hevc）。失败返回空字符串。
     */
    public static String probeVideoCodecName(java.nio.file.Path media) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                "ffprobe", "-v", "error",
                "-select_streams", "v:0",
                "-show_entries", "stream=codec_name",
                "-of", "default=nk=1:nw=1",
                media.toAbsolutePath().toString()
        );
        Process p = pb.start();
        String line = null;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            line = r.readLine();
        }
        p.waitFor();
        return line == null ? "" : line.trim().toLowerCase();
    }

    /**
     * 使用 ffmpeg 抽取视频第一帧并保存为图片。
     *
     * @param media       输入视频文件路径
     * @param outputImage 输出图片路径（如 .jpg 或 .png），已存在则会被覆盖
     * @return 抽帧是否成功
     */
    public static boolean extractFirstFrame(java.nio.file.Path media, java.nio.file.Path outputImage) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-y",
                "-i", media.toAbsolutePath().toString(),
                "-frames:v", "1",
                "-q:v", "2",
                outputImage.toAbsolutePath().toString()
        );
        // 丢弃 ffmpeg 日志输出，避免占满缓冲区
        pb.redirectError(ProcessBuilder.Redirect.DISCARD);
        Process p = pb.start();
        int exit = p.waitFor();
        return exit == 0 && Files.exists(outputImage);
    }
}


