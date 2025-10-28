package com.zhongjia.subtitlefusion.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 调用 ffprobe 获取媒体信息的简易工具。
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
}


