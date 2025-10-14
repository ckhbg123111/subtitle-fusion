package com.zhongjia.subtitlefusion.service;

import com.zhongjia.subtitlefusion.config.AppProperties;
import com.zhongjia.subtitlefusion.model.TaskState;
import com.zhongjia.subtitlefusion.model.VideoChainRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class VideoChainFFmpegService {

    private final DistributedTaskManagementService tasks;
    private final FileDownloadService downloader;
    private final MinioService minio;
    private final AppProperties props;

    public VideoChainFFmpegService(DistributedTaskManagementService tasks,
                                   FileDownloadService downloader,
                                   MinioService minio,
                                   AppProperties props) {
        this.tasks = tasks;
        this.downloader = downloader;
        this.minio = minio;
        this.props = props;
    }

    @Async("subtitleTaskExecutor")
    public void processAsync(VideoChainRequest req) {
        String taskId = req.getTaskId();
        List<Path> tempFiles = new ArrayList<>();
        List<Path> segmentOutputs = new ArrayList<>();
        try {
            tasks.updateTaskProgress(taskId, TaskState.DOWNLOADING, 5, "开始下载素材");

            Path workDir = ensureWorkDir(taskId);

            int segIdx = 0;
            for (VideoChainRequest.SegmentInfo seg : req.getSegmentList()) {
                segIdx++;

                // 1) 下载小视频并写 list.txt
                List<Path> segVideos = new ArrayList<>();
                if (seg.getVideoInfos() != null) {
                    for (VideoChainRequest.VideoInfo vi : seg.getVideoInfos()) {
                        Path p = downloader.downloadVideo(vi.getVideoUrl());
                        segVideos.add(p); tempFiles.add(p);
                    }
                }
                Path segList = workDir.resolve("segment_" + segIdx + "_list.txt");
                writeConcatList(segList, segVideos);
                tempFiles.add(segList);
                
                // 2) 无声拼接
                Path segNoSound = workDir.resolve("segment_" + segIdx + "_nosound.mp4");
                execFfmpeg(new String[]{
                        "ffmpeg", "-y",
                        "-f", "concat", "-safe", "0",
                        "-i", segList.toString(),
                        "-c", "copy",
                        segNoSound.toString()
                }, line -> tasks.updateTaskProgress(taskId, TaskState.PROCESSING, 20, "段内无声拼接"));
                tempFiles.add(segNoSound);

                // 3) 下载音频/字幕/插图
                Path audio = seg.getAudioUrl() != null ? downloader.downloadFile(seg.getAudioUrl(), ".m4a") : null;
                if (audio != null) tempFiles.add(audio);

                Path srt = null;
                if (seg.getSrtUrl() != null && !seg.getSrtUrl().isEmpty()) {
                    srt = downloader.downloadSubtitle(seg.getSrtUrl());
                }
                if (srt != null) tempFiles.add(srt);

                List<Path> pictures = new ArrayList<>();
                if (seg.getPictureInfos() != null) {
                    for (VideoChainRequest.PictureInfo pi : seg.getPictureInfos()) {
                        Path pic = downloader.downloadFile(pi.getPictureUrl(), guessExt(pi.getPictureUrl(), ".png"));
                        pictures.add(pic); tempFiles.add(pic);
                    }
                }

                // 4) 构建滤镜与输入
                List<String> cmd = new ArrayList<>();
                cmd.add("ffmpeg"); cmd.add("-y");
                cmd.add("-i"); cmd.add(segNoSound.toString());
                if (audio != null) { cmd.add("-i"); cmd.add(audio.toString()); }
                for (Path p : pictures) { cmd.add("-i"); cmd.add(p.toString()); }

                boolean hasAudio = audio != null;
                String filter = buildFilterChain(seg, pictures, srt, hasAudio);
                if (!filter.isEmpty()) {
                    cmd.add("-filter_complex"); cmd.add(filter);
                    cmd.add("-map"); cmd.add("[vout]");
                } else {
                    cmd.add("-map"); cmd.add("0:v");
                }
                if (audio != null) { cmd.add("-map"); cmd.add("1:a"); }
                cmd.add("-c:v"); cmd.add("libx264");
                if (audio != null) { cmd.add("-c:a"); cmd.add("aac"); }

                Path segOut = workDir.resolve("segment_" + segIdx + ".mp4");
                cmd.add(segOut.toString());

                execFfmpeg(cmd.toArray(new String[0]), line -> tasks.updateTaskProgress(taskId, TaskState.PROCESSING, 60, "段内滤镜与字幕"));
                segmentOutputs.add(segOut);
            }

            // 5) 段间拼接
            tasks.updateTaskProgress(taskId, TaskState.PROCESSING, 70, "段间拼接");
            Path concatList = workDir.resolve("concat_all.txt");
            try (FileOutputStream fos = new FileOutputStream(concatList.toFile())) {
                for (Path p : segmentOutputs) {
                    String line = "file '" + p.toAbsolutePath().toString().replace("\\", "/") + "'\n";
                    fos.write(line.getBytes(StandardCharsets.UTF_8));
                }
            }
            Path finalOut = workDir.resolve("final_" + taskId + ".mp4");
            execFfmpeg(new String[]{
                    "ffmpeg", "-y",
                    "-f", "concat", "-safe", "0",
                    "-i", concatList.toString(),
                    "-c", "copy",
                    finalOut.toString()
            }, line -> tasks.updateTaskProgress(taskId, TaskState.PROCESSING, 85, "拼接完成"));

            // 6) 上传
            tasks.updateTaskProgress(taskId, TaskState.UPLOADING, 90, "上传到对象存储");
            String objectUrl = minio.uploadToPublicBucket(new FileInputStream(finalOut.toFile()), finalOut.toFile().length(), finalOut.getFileName().toString());
            tasks.markTaskCompleted(taskId, objectUrl);
            safeDelete(finalOut);

        } catch (Exception ex) {
            tasks.markTaskFailed(req.getTaskId(), ex.getMessage());
        } finally {
            // 清理临时
            for (Path p : tempFiles) safeDelete(p);
        }
    }

    private Path ensureWorkDir(String taskId) throws Exception {
        Path temp = Paths.get(props.getTempDir());
        Files.createDirectories(temp);
        Path dir = temp.resolve("videochain_" + taskId + "_" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()));
        Files.createDirectories(dir);
        return dir;
    }

    private void writeConcatList(Path listFile, List<Path> files) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(listFile.toFile())) {
            for (Path p : files) {
                String line = "file '" + p.toAbsolutePath().toString().replace("\\", "/") + "'\n";
                fos.write(line.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    private String buildFilterChain(VideoChainRequest.SegmentInfo seg, List<Path> pictures, Path srt, boolean hasAudio) {
        boolean hasPics = pictures != null && !pictures.isEmpty();
        boolean hasKeywords = seg.getKeywordsInfos() != null && !seg.getKeywordsInfos().isEmpty();
        boolean hasSrt = srt != null;
        if (!hasPics && !hasKeywords && !hasSrt) {
            return ""; // 没有任何滤镜
        }
        List<String> chains = new ArrayList<>();
        String last = "[0:v]";
        int picBaseIndex = hasAudio ? 2 : 1; // 0:v (+1:a) 之后的图片输入索引
        for (int i = 0; i < (pictures != null ? pictures.size() : 0); i++) {
            int inIndex = picBaseIndex + i;
            VideoChainRequest.PictureInfo pi = seg.getPictureInfos().get(i);
            String[] overlayExpr = buildOverlayExpr(pi);
            String out = tag();
            chains.add(last + "[" + inIndex + ":v]overlay=x=" + overlayExpr[0] + ":y=" + overlayExpr[1] + ":enable='between(t," + toSeconds(pi.getStartTime()) + "," + toSeconds(pi.getEndTime()) + ")'" + out);
            last = out;
        }
        if (seg.getKeywordsInfos() != null) {
            for (VideoChainRequest.KeywordsInfo ki : seg.getKeywordsInfos()) {
                String font = props.getRender().getFontFile() != null && !props.getRender().getFontFile().isEmpty()
                        ? ":fontfile='" + props.getRender().getFontFile().replace("\\", "/") + "'"
                        : ":fontfile='/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttc'";
                String color = "white";
                String pos = ki.getPosition() == VideoChainRequest.Position.Left ? "x=(w-tw)/6:y=h*0.85-th" : "x=w-tw-(w*0.05):y=h*0.85-th";
                String out = tag();
                chains.add(last + "drawtext=text='" + escapeText(ki.getKeyword()) + "'" + font + ":fontcolor=" + color + ":fontsize=h*0.04:shadowx=2:shadowy=2:shadowcolor=black@0.7:" + pos + ":enable='between(t," + toSeconds(ki.getStartTime()) + "," + toSeconds(ki.getEndTime()) + ")'" + out);
                last = out;
            }
        }
        if (srt != null) {
            String style = "force_style='FontName=" + safe(props.getRender().getFontFamily(), "Microsoft YaHei") + ",FontSize=18,Outline=1,Shadow=1'";
            chains.add(last + "subtitles='" + srt.toAbsolutePath().toString().replace("\\", "/") + "':" + style + "[vout]");
        } else {
            chains.add(last + "format=yuv420p[vout]");
        }
        return String.join(";", chains);
    }

    private String[] buildOverlayExpr(VideoChainRequest.PictureInfo pi) {
        // Left/right 简化：5% 边距，垂直居中
        String x = (pi.getPosition() == VideoChainRequest.Position.Left) ? "W*0.05" : "W-w-W*0.05";
        String y = "(H-h)/2";
        return new String[]{x, y};
    }

    private String guessExt(String url, String def) {
        try {
            String path = url.split("\\?")[0];
            int i = path.lastIndexOf('.');
            if (i > 0 && i < path.length() - 1) return path.substring(i);
        } catch (Exception ignored) {}
        return def;
    }

    private void execFfmpeg(String[] cmd, java.util.function.Consumer<String> onLine) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process p = pb.start();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String line; while ((line = r.readLine()) != null) { if (onLine != null) onLine.accept(line); }
        }
        int code = p.waitFor();
        if (code != 0) throw new RuntimeException("FFmpeg 执行失败, code=" + code);
    }

    private void safeDelete(Path p) {
        try { if (p != null) Files.deleteIfExists(p); } catch (Exception ignored) {}
    }

    private String tag() { return "[v" + UUID.randomUUID().toString().replace("-", "").substring(0, 6) + "]"; }

    private String escapeText(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("'", "\\'").replace(":", "\\:");
    }

    private String toSeconds(String t) {
        if (t == null || t.isEmpty()) return "0";
        try {
            if (t.contains(":")) {
                String[] ps = t.split(":");
                double h = Double.parseDouble(ps[0]);
                double m = Double.parseDouble(ps[1]);
                double s = Double.parseDouble(ps[2]);
                return String.format(Locale.US, "%.3f", h * 3600 + m * 60 + s);
            }
            return String.format(Locale.US, "%.3f", Double.parseDouble(t));
        } catch (Exception e) {
            return "0";
        }
    }

    private String safe(String v, String def) {
        return (v == null || v.isEmpty()) ? def : v;
    }
}


