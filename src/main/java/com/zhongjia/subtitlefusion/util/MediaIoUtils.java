package com.zhongjia.subtitlefusion.util;

import com.zhongjia.subtitlefusion.config.AppProperties;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 与媒体相关的简易 IO 工具。
 */
public final class MediaIoUtils {

    private MediaIoUtils() {}

    public static Path ensureWorkDir(AppProperties props, String taskId) throws Exception {
        Path temp = Paths.get(props.getTempDir());
        Files.createDirectories(temp);
        Path dir = temp.resolve("videochain_" + taskId + "_" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()));
        Files.createDirectories(dir);
        return dir;
    }

    public static void writeConcatList(Path listFile, List<Path> files) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(listFile.toFile())) {
            for (Path p : files) {
                String line = "file '" + p.toAbsolutePath().toString().replace("\\", "/") + "'\n";
                fos.write(line.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    public static String guessExt(String url, String def) {
        try {
            String path = url.split("\\?")[0];
            int i = path.lastIndexOf('.');
            if (i > 0 && i < path.length() - 1) return path.substring(i);
        } catch (Exception ignored) {}
        return def;
    }

    public static void safeDelete(Path p) {
        try { if (p != null) Files.deleteIfExists(p); } catch (Exception ignored) {}
    }
}


