package com.zhongjia.subtitlefusion.service;

import com.zhongjia.subtitlefusion.config.AppProperties;
import com.zhongjia.subtitlefusion.ffmpeg.FFmpegExecutor;
import com.zhongjia.subtitlefusion.ffmpeg.FilterChainBuilder;
import com.zhongjia.subtitlefusion.model.TaskState;
import com.zhongjia.subtitlefusion.model.VideoChainRequest;
import com.zhongjia.subtitlefusion.util.MediaIoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class VideoChainFFmpegService {

    private static final Logger log = LoggerFactory.getLogger(VideoChainFFmpegService.class);

    private final DistributedTaskManagementService tasks;
    private final FileDownloadService downloader;
    private final MinioService minio;
    private final AppProperties props;
    private final FFmpegExecutor ffmpegExecutor;
    private final FilterChainBuilder filterChainBuilder;

    public VideoChainFFmpegService(DistributedTaskManagementService tasks,
                                   FileDownloadService downloader,
                                   MinioService minio,
                                   AppProperties props,
                                   FFmpegExecutor ffmpegExecutor,
                                   FilterChainBuilder filterChainBuilder) {
        this.tasks = tasks;
        this.downloader = downloader;
        this.minio = minio;
        this.props = props;
        this.ffmpegExecutor = ffmpegExecutor;
        this.filterChainBuilder = filterChainBuilder;
    }

    @Async("subtitleTaskExecutor")
    public void processAsync(VideoChainRequest req) {
        String taskId = req.getTaskId();
        List<Path> tempFiles = new ArrayList<>();
        List<Path> segmentOutputs = new ArrayList<>();
        try {
            tasks.updateTaskProgress(taskId, TaskState.DOWNLOADING, 5, "开始下载素材");

            Path workDir = MediaIoUtils.ensureWorkDir(props, taskId);

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
                MediaIoUtils.writeConcatList(segList, segVideos);
                tempFiles.add(segList);
                
                // 2) 无声拼接
                Path segNoSound = workDir.resolve("segment_" + segIdx + "_nosound.mp4");
                // 阶段性单次进度更新（避免按行日志导致重复输出）
                tasks.updateTaskProgress(taskId, TaskState.PROCESSING, 20, "段内无声拼接");
                ffmpegExecutor.exec(new String[]{
                        "ffmpeg", "-y",
                        "-f", "concat", "-safe", "0",
                        "-i", segList.toString(),
                        "-c", "copy",
                        segNoSound.toString()
                }, null);
                tempFiles.add(segNoSound);

                // 3) 下载音频/字幕/插图/SVG
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
                        Path pic = downloader.downloadFile(pi.getPictureUrl(), MediaIoUtils.guessExt(pi.getPictureUrl(), ".png"));
                        pictures.add(pic); tempFiles.add(pic);
                    }
                }

                List<Path> svgs = new ArrayList<>();
                if (seg.getSvgInfos() != null) {
                    for (VideoChainRequest.SvgInfo si : seg.getSvgInfos()) {
                        Path svg = downloader.downloadFile(si.getSvgUrl(), MediaIoUtils.guessExt(si.getSvgUrl(), ".svg"));
                        svgs.add(svg); tempFiles.add(svg);
                    }
                }

                // 4) 构建滤镜与输入
                List<String> cmd = new ArrayList<>();
                cmd.add("ffmpeg"); cmd.add("-y");
                cmd.add("-i"); cmd.add(segNoSound.toString());
                if (audio != null) { cmd.add("-i"); cmd.add(audio.toString()); }
                for (Path p : pictures) { cmd.add("-i"); cmd.add(p.toString()); }
                for (Path p : svgs) { cmd.add("-i"); cmd.add(p.toString()); }

                boolean hasAudio = audio != null;
                String filter = filterChainBuilder.buildFilterChain(seg, pictures, svgs, srt, hasAudio);
                if (log.isDebugEnabled()) {
                    log.debug("FFmpeg filter_complex: {}", filter);
                }
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

                // 阶段性单次进度更新（避免按行日志导致重复输出）
                tasks.updateTaskProgress(taskId, TaskState.PROCESSING, 60, "段内滤镜与字幕");
                ffmpegExecutor.exec(cmd.toArray(new String[0]), null);
                segmentOutputs.add(segOut);
            }

            // 5) 段间拼接
            tasks.updateTaskProgress(taskId, TaskState.PROCESSING, 70, "段间拼接");
            Path concatList = workDir.resolve("concat_all.txt");
            MediaIoUtils.writeConcatList(concatList, segmentOutputs);
            Path finalOut = workDir.resolve("final_" + taskId + ".mp4");
            ffmpegExecutor.exec(new String[]{
                    "ffmpeg", "-y",
                    "-f", "concat", "-safe", "0",
                    "-i", concatList.toString(),
                    "-c", "copy",
                    finalOut.toString()
            }, null);
            tasks.updateTaskProgress(taskId, TaskState.PROCESSING, 85, "拼接完成");

            // 6) 上传
            tasks.updateTaskProgress(taskId, TaskState.UPLOADING, 90, "上传到对象存储");
            String objectUrl = minio.uploadToPublicBucket(new FileInputStream(finalOut.toFile()), finalOut.toFile().length(), finalOut.getFileName().toString());
            tasks.markTaskCompleted(taskId, objectUrl);
            MediaIoUtils.safeDelete(finalOut);

        } catch (Exception ex) {
            tasks.markTaskFailed(req.getTaskId(), ex.getMessage());
        } finally {
            // 清理临时
            for (Path p : tempFiles) MediaIoUtils.safeDelete(p);
        }
    }
}


