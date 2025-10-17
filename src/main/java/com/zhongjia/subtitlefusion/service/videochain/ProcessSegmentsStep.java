package com.zhongjia.subtitlefusion.service.videochain;

import com.zhongjia.subtitlefusion.ffmpeg.FFmpegExecutor;
import com.zhongjia.subtitlefusion.ffmpeg.FilterChainBuilder;
import com.zhongjia.subtitlefusion.model.TaskState;
import com.zhongjia.subtitlefusion.model.VideoChainRequest;
import com.zhongjia.subtitlefusion.service.DistributedTaskManagementService;
import com.zhongjia.subtitlefusion.service.FileDownloadService;
import com.zhongjia.subtitlefusion.util.MediaIoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Component
@Order(20)
public class ProcessSegmentsStep implements VideoChainStep {

    private static final Logger log = LoggerFactory.getLogger(ProcessSegmentsStep.class);

    @Autowired
    private DistributedTaskManagementService tasks;
    @Autowired
    private FileDownloadService downloader;
    @Autowired
    private FFmpegExecutor ffmpegExecutor;
    @Autowired
    private FilterChainBuilder filterChainBuilder;

    @Override
    public String name() {
        return "ProcessSegments";
    }

    @Override
    public void execute(VideoChainContext ctx) throws Exception {
        String taskId = ctx.getTaskId();
        Path workDir = ctx.getWorkDir();

        int segIdx = 0;
        for (VideoChainRequest.SegmentInfo seg : ctx.getRequest().getSegmentList()) {
            segIdx++;

            List<Path> tempFiles = ctx.getTempFiles();

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
            if (audio != null) { tempFiles.add(audio); ctx.setAnySegHasAudio(true); }

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
                    if (pi.getImageBorderUrl() != null && !pi.getImageBorderUrl().isEmpty()) {
                        Path border = downloader.downloadFile(pi.getImageBorderUrl(), MediaIoUtils.guessExt(pi.getImageBorderUrl(), ".png"));
                        pictures.add(border); tempFiles.add(border);
                    }
                }
            }

            List<Path> svgs = new ArrayList<>();
            if (seg.getSvgInfos() != null) {
                int svgIdx = 0;
                for (VideoChainRequest.SvgInfo si : seg.getSvgInfos()) {
                    svgIdx++;
                    if (si.getSvgBase64() == null || si.getSvgBase64().isEmpty()) continue;
                    Path svg = writeSvgFromBase64(workDir, si.getSvgBase64(), svgIdx);
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

            tasks.updateTaskProgress(taskId, TaskState.PROCESSING, 60, "段内滤镜与字幕");
            ffmpegExecutor.exec(cmd.toArray(new String[0]), null);
            ctx.getSegmentOutputs().add(segOut);
        }
    }

    private Path writeSvgFromBase64(Path workDir, String base64, int index) throws Exception {
        String raw = base64;
        int comma = raw.indexOf(',');
        if (comma >= 0) {
            raw = raw.substring(comma + 1);
        }
        byte[] bytes = Base64.getDecoder().decode(raw);
        Path svg = workDir.resolve("svg_" + index + ".svg");
        Files.write(svg, bytes);
        return svg;
    }
}


