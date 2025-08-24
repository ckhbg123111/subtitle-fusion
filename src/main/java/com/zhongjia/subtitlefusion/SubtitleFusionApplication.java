package com.zhongjia.subtitlefusion;

import com.zhongjia.subtitlefusion.config.AppProperties;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.bytedeco.javacpp.Loader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class SubtitleFusionApplication {

    public static void main(String[] args) {
        // 开启 FFmpeg 日志，便于定位 filtergraph 细节错误
        FFmpegLogCallback.set();

        // 预加载需要的原生库，确保 subtitles 滤镜可用
        try {
            Loader.load(org.bytedeco.ffmpeg.global.avfilter.class);

        } catch (Throwable t) {
            // 若加载失败，继续启动但保留日志提示
            System.err.println("Native preload failed: " + t.getMessage());
        }
        SpringApplication.run(SubtitleFusionApplication.class, args);
    }

}
