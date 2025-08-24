package com.zhongjia.subtitlefusion.service;

import com.zhongjia.subtitlefusion.config.AppProperties;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 视频处理服务
 * 负责视频帧的处理和字幕合成
 */
@Service
public class VideoProcessingService {

    private final AppProperties appProperties;
    private final SubtitleRendererService rendererService;

    public VideoProcessingService(AppProperties appProperties, SubtitleRendererService rendererService) {
        this.appProperties = appProperties;
        this.rendererService = rendererService;
    }

    /**
     * 使用Java2D将字幕渲染到视频中
     * @param videoPath 视频文件路径
     * @param cues 字幕条目列表
     * @param baseFileName 输出文件基础名称
     * @return 输出文件路径
     */
    public String processVideoWithSubtitles(Path videoPath, List<SubtitleParserService.SrtCue> cues, String baseFileName) throws Exception {
        System.out.println("开始使用Java2D方式渲染字幕...");
        System.out.println("视频文件: " + videoPath);

        Path outputDir = Paths.get(appProperties.getOutputDir());
        Files.createDirectories(outputDir);

        String ext = getExt(videoPath.getFileName().toString());
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path outputPath = outputDir.resolve(baseFileName + "_sub_srt2d_" + timestamp + ext);

        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath.toString())) {
            grabber.start();

            int width = grabber.getImageWidth();
            int height = grabber.getImageHeight();
            int audioChannels = Math.max(0, grabber.getAudioChannels());
            double fr = grabber.getVideoFrameRate();
            int srcVideoBitrate = Math.max(0, grabber.getVideoBitrate());
            int srcAudioBitrate = Math.max(0, grabber.getAudioBitrate());
            int srcSampleRate = grabber.getSampleRate() > 0 ? grabber.getSampleRate() : 44100;

            try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputPath.toString(), width, height, audioChannels)) {
                setupRecorder(recorder, grabber, fr, srcVideoBitrate, srcAudioBitrate, srcSampleRate, audioChannels);
                recorder.start();

                try (Java2DFrameConverter converter = new Java2DFrameConverter()) {
                    processFrames(grabber, recorder, converter, cues, fr);
                }

                recorder.stop();
            }
        }

        System.out.println("Java2D字幕渲染完成: " + outputPath);
        return outputPath.toAbsolutePath().toString();
    }

    /**
     * 配置录制器参数
     */
    private void setupRecorder(FFmpegFrameRecorder recorder, FFmpegFrameGrabber grabber, 
                              double fr, int srcVideoBitrate, int srcAudioBitrate, 
                              int srcSampleRate, int audioChannels) {
        recorder.setFormat("mp4");
        
        int srcVideoCodec = grabber.getVideoCodec();
        recorder.setVideoCodec(srcVideoCodec > 0 ? srcVideoCodec : avcodec.AV_CODEC_ID_H264);
        
        if (audioChannels > 0) {
            int srcAudioCodec = grabber.getAudioCodec();
            recorder.setAudioCodec(srcAudioCodec > 0 ? srcAudioCodec : avcodec.AV_CODEC_ID_AAC);
            recorder.setSampleRate(srcSampleRate);
            recorder.setAudioBitrate(srcAudioBitrate > 0 ? srcAudioBitrate : 128_000);
        }
        
        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
        
        if (fr > 0) recorder.setFrameRate(fr);
        
        // 关键：使用与源接近的码率与合理 GOP，提升清晰度
        recorder.setVideoBitrate(srcVideoBitrate > 0 ? srcVideoBitrate : 5_000_000);
        
        if (fr > 0) {
            int gop = (int) Math.max(12, Math.round(fr * 2));
            recorder.setGopSize(gop);
        }
    }

    /**
     * 处理视频帧并添加字幕
     */
    private void processFrames(FFmpegFrameGrabber grabber, FFmpegFrameRecorder recorder,
                              Java2DFrameConverter converter, List<SubtitleParserService.SrtCue> cues,
                              double fr) throws Exception {
        Frame frame;
        long lastTimestamp = -1; // 记录上一帧的时间戳
        
        while ((frame = grabber.grabFrame()) != null) {
            if (frame.image != null) {
                long ts = frame.timestamp; // 微秒
                BufferedImage img = converter.getBufferedImage(frame);
                
                if (img != null) {
                    rendererService.drawSrtOnImage(img, ts, cues);
                    Frame out = converter.getFrame(img);

                    // 确保时间戳单调递增
                    if (ts > 0 && ts > lastTimestamp) {
                        recorder.setTimestamp(ts);
                        lastTimestamp = ts;
                    } else if (lastTimestamp >= 0) {
                        // 如果当前时间戳无效或不递增，则使用递增的时间戳
                        double frameRate = fr > 0 ? fr : 25.0; // 默认帧率25fps
                        lastTimestamp += (long) (1000000.0 / frameRate); // 根据帧率计算下一帧时间戳
                        recorder.setTimestamp(lastTimestamp);
                    }

                    recorder.record(out);
                }
            } else if (frame.samples != null) {
                recorder.record(frame);
            }
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getExt(String name) {
        int i = name.lastIndexOf('.');
        return i > 0 ? name.substring(i) : ".mp4";
    }

    /**
     * 去除文件扩展名
     */
    public String stripExt(String name) {
        int i = name.lastIndexOf('.');
        return i > 0 ? name.substring(0, i) : name;
    }
}
