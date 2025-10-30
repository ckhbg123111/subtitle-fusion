package com.zhongjia.subtitlefusion.service;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字幕解析服务
 * 负责SRT字幕文件的解析、编码检测和转换
 */
@Service
@Slf4j
public class SubtitleParserService {

    /**
     * SRT字幕条目
     */
    public static class SrtCue {
        public long startUs;
        public long endUs;
        public List<String> lines;
    }

    /**
     * 解析SRT字幕文件
     * @param srtPath 字幕文件路径
     * @return 解析后的字幕条目列表
     */
    public List<SrtCue> parseSrtFile(Path srtPath) throws IOException {
        // 确保字幕文件是UTF-8编码
        Path utf8SrtPath = ensureUtf8Encoding(srtPath);
        List<String> lines = Files.readAllLines(utf8SrtPath, StandardCharsets.UTF_8);
        List<SrtCue> cues = parseSrt(lines);
        
        // 清理临时文件
        if (!utf8SrtPath.equals(srtPath)) {
            try {
                Files.deleteIfExists(utf8SrtPath);
            } catch (Exception e) {
                log.warn("清理临时字幕文件失败: {}", e.getMessage());
            }
        }
        
        log.info("解析到 {} 个字幕条目", cues.size());
        return cues;
    }

    /**
     * 解析SRT格式字幕内容
     */
    private List<SrtCue> parseSrt(List<String> lines) {
        List<SrtCue> cues = new ArrayList<>();
        Pattern timePat = Pattern.compile("(\\d{2}):(\\d{2}):(\\d{2}),(\\d{3})\\s*-->\\s*(\\d{2}):(\\d{2}):(\\d{2}),(\\d{3})");
        int i = 0;
        while (i < lines.size()) {
            // 跳过序号行
            while (i < lines.size() && lines.get(i).trim().isEmpty()) i++;
            if (i < lines.size() && lines.get(i).trim().matches("\\d+")) i++;
            if (i >= lines.size()) break;

            Matcher m = timePat.matcher(lines.get(i).trim());
            if (!m.matches()) {
                i++;
                continue;
            }
            long startUs = hmsToUs(m.group(1), m.group(2), m.group(3), m.group(4));
            long endUs = hmsToUs(m.group(5), m.group(6), m.group(7), m.group(8));
            i++;

            List<String> text = new ArrayList<>();
            while (i < lines.size() && !lines.get(i).trim().isEmpty()) {
                text.add(lines.get(i));
                i++;
            }
            SrtCue cue = new SrtCue();
            cue.startUs = startUs;
            cue.endUs = endUs;
            cue.lines = text;
            cues.add(cue);
        }
        return cues;
    }

    /**
     * 时间转换为微秒
     */
    private long hmsToUs(String hh, String mm, String ss, String ms) {
        long h = Long.parseLong(hh);
        long m = Long.parseLong(mm);
        long s = Long.parseLong(ss);
        long milli = Long.parseLong(ms);
        return ((h * 3600 + m * 60 + s) * 1000 + milli) * 1000;
    }

    /**
     * 确保字幕文件是UTF-8编码，如果不是则转换
     */
    private Path ensureUtf8Encoding(Path subtitlePath) throws IOException {
        // 首先尝试用UTF-8读取
        try {
            List<String> lines = Files.readAllLines(subtitlePath, StandardCharsets.UTF_8);
            // 检查是否包含乱码或无效字符
            boolean hasValidContent = lines.stream()
                    .anyMatch(line -> line.trim().length() > 0 && !containsInvalidChars(line));

            if (hasValidContent) {
                return subtitlePath; // 已经是UTF-8编码
            }
        } catch (Exception e) {
            log.warn("UTF-8读取失败，尝试其他编码: {}", e.getMessage());
        }

        // 尝试其他常见编码
        Charset[] encodings = {
                Charset.forName("GBK"),
                Charset.forName("GB2312"),
                Charset.forName("Big5"),
                StandardCharsets.ISO_8859_1
        };

        for (Charset charset : encodings) {
            try {
                List<String> lines = Files.readAllLines(subtitlePath, charset);
                boolean hasValidContent = lines.stream()
                        .anyMatch(line -> line.trim().length() > 0 && !containsInvalidChars(line));

                if (hasValidContent) {
                    // 找到正确编码，转换为UTF-8
                    log.info("检测到字幕文件编码: {}，正在转换为UTF-8", charset.name());
                    return convertToUtf8(subtitlePath, lines);
                }
            } catch (Exception e) {
                // 继续尝试下一个编码
            }
        }

        // 如果所有编码都失败，使用原文件（可能已经是UTF-8但有特殊字符）
        log.info("无法确定字幕文件编码，使用原始文件");
        return subtitlePath;
    }

    /**
     * 检查字符串是否包含无效字符
     */
    private boolean containsInvalidChars(String line) {
        // 检查是否包含常见的乱码字符
        return line.contains("�") ||
                line.contains("锘?") ||
                line.matches(".*[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F].*");
    }

    /**
     * 将字幕内容转换为UTF-8编码的临时文件
     */
    private Path convertToUtf8(Path originalPath, List<String> lines) throws IOException {
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
        String fileName = originalPath.getFileName().toString();
        String baseName = stripExt(fileName);
        String ext = getExt(fileName);

        Path utf8Path = tempDir.resolve(baseName + "_utf8_" + System.currentTimeMillis() + ext);
        Files.write(utf8Path, lines, StandardCharsets.UTF_8);

        log.info("已转换字幕文件为UTF-8编码: {}", utf8Path);
        return utf8Path;
    }

    private String stripExt(String name) {
        int i = name.lastIndexOf('.');
        return i > 0 ? name.substring(0, i) : name;
    }

    private String getExt(String name) {
        int i = name.lastIndexOf('.');
        return i > 0 ? name.substring(i) : ".srt";
    }
}
