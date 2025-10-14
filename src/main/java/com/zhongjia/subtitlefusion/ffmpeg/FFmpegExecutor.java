package com.zhongjia.subtitlefusion.ffmpeg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

/**
 * 封装 FFmpeg 命令执行与日志尾部收集。
 */
@Component
public class FFmpegExecutor {

    private static final Logger log = LoggerFactory.getLogger(FFmpegExecutor.class);

    /**
     * 执行 FFmpeg 命令。
     * @param cmd 命令及参数
     * @param onLine 可选的逐行回调（stderr 已合并）
     */
    public void exec(String[] cmd, Consumer<String> onLine) throws Exception {
        String cmdLine = formatCommand(cmd);
        log.info("执行 FFmpeg: {}", cmdLine);

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process p = pb.start();

        Deque<String> tail = new ArrayDeque<>();
        final int tailLimit = 80;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (onLine != null) onLine.accept(line);
                if (log.isDebugEnabled()) log.debug("ffmpeg> {}", line);
                tail.addLast(line);
                while (tail.size() > tailLimit) tail.removeFirst();
            }
        }

        int code = p.waitFor();
        if (code != 0) {
            StringBuilder sb = new StringBuilder();
            for (String s : tail) sb.append(s).append('\n');
            String message = "FFmpeg 执行失败, code=" + code + "\n命令: " + cmdLine + "\n输出尾部:\n" + sb;
            log.warn(message);
            throw new RuntimeException(message);
        }
        log.info("FFmpeg 执行完成");
    }

    private String formatCommand(String[] cmd) {
        StringBuilder sb = new StringBuilder();
        for (String s : cmd) {
            if (s == null) continue;
            boolean needQuote = s.contains(" ") || s.contains("\"") || s.contains("(") || s.contains(")");
            if (needQuote) {
                sb.append('"').append(s.replace("\"", "\\\"")).append('"');
            } else {
                sb.append(s);
            }
            sb.append(' ');
        }
        return sb.toString().trim();
    }
}


