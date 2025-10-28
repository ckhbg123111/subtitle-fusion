package com.zhongjia.subtitlefusion.service;

import com.zhongjia.subtitlefusion.ffmpeg.FFmpegExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 将若干短音效在指定时间点混入视频音轨。
 * 简化版本：若无原音频，则直接以空白基底叠加；目前假设原视频有音轨。
 */
@Service
public class AudioEffectMergeService {

    @Autowired
    private FFmpegExecutor ffmpeg;

    /**
     * 将 effectAudios 以 amix 混入源视频。
     * @param srcVideo 输入视频
     * @param effectAudios 音效文件路径列表（本简化版本不处理入场时刻对齐，后续可扩展 asetpts + adelay）
     * @return 输出视频路径
     */
    public Path mixAudioEffects(Path srcVideo, List<Path> effectAudios) throws Exception {
        if (effectAudios == null || effectAudios.isEmpty()) return srcVideo;
        Path out = Files.createTempFile("mix_fx_", ".mp4");

        List<String> cmd = new ArrayList<>();
        cmd.add("ffmpeg"); cmd.add("-y");
        cmd.add("-i"); cmd.add(srcVideo.toString());
        for (Path a : effectAudios) { cmd.add("-i"); cmd.add(a.toString()); }

        int inputs = 1 + effectAudios.size();
        StringBuilder fc = new StringBuilder();
        // 将所有音频输入合并到 [am]
        fc.append("[0:a]");
        for (int i = 1; i < inputs; i++) {
            fc.append("[").append(i).append(":a]");
        }
        fc.append("amix=inputs=").append(inputs).append(":duration=first:dropout_transition=2[am]");

        cmd.add("-filter_complex"); cmd.add(fc.toString());
        cmd.add("-map"); cmd.add("0:v");
        cmd.add("-map"); cmd.add("[am]");
        cmd.add("-c:v"); cmd.add("libx264");
        cmd.add("-c:a"); cmd.add("aac");
        cmd.add(out.toString());

        ffmpeg.exec(cmd.toArray(new String[0]), null);
        return out;
    }
}


