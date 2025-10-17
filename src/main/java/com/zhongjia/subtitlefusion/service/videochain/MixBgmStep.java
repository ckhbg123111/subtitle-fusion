package com.zhongjia.subtitlefusion.service.videochain;

import com.zhongjia.subtitlefusion.ffmpeg.FFmpegExecutor;
import com.zhongjia.subtitlefusion.model.TaskState;
import com.zhongjia.subtitlefusion.service.DistributedTaskManagementService;
import com.zhongjia.subtitlefusion.util.MediaIoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
@Order(40)
public class MixBgmStep implements VideoChainStep {

    @Autowired
    private DistributedTaskManagementService tasks;
    @Autowired
    private FFmpegExecutor ffmpegExecutor;

    @Override
    public String name() {
        return "MixBgm";
    }

    @Override
    public void execute(VideoChainContext ctx) throws Exception {
        if (ctx.getBgm() == null) {
            return; // 跳过
        }

        String taskId = ctx.getTaskId();
        tasks.updateTaskProgress(taskId, TaskState.PROCESSING, 88, "混入背景音乐");

        Path workDir = ctx.getWorkDir();
        Path finalOut = ctx.getFinalOut();
        Path bgm = ctx.getBgm();

        double vol = (ctx.getBgmVolume() == null || ctx.getBgmVolume() <= 0.0) ? 0.25 : ctx.getBgmVolume();
        double fin = (ctx.getBgmFadeInSec() == null || ctx.getBgmFadeInSec() <= 0.0) ? 0.0 : ctx.getBgmFadeInSec();
        double fout = (ctx.getBgmFadeOutSec() == null || ctx.getBgmFadeOutSec() <= 0.0) ? 0.0 : ctx.getBgmFadeOutSec();

        Path finalWithBgm = workDir.resolve("final_bgm_" + taskId + ".mp4");

        List<String> mix = new ArrayList<>();
        mix.add("ffmpeg"); mix.add("-y");
        mix.add("-i"); mix.add(finalOut.toString());
        mix.add("-stream_loop"); mix.add("-1");
        mix.add("-i"); mix.add(bgm.toString());

        StringBuilder fc = new StringBuilder();
        fc.append("[1:a]volume=").append(vol);
        if (fin > 0.0) {
            fc.append(",afade=t=in:st=0:d=").append(fin);
        }
        fc.append("[a_bgm]");

        if (ctx.isAnySegHasAudio()) {
            fc.append(";[a_bgm][0:a]sidechaincompress=threshold=0.05:ratio=8:attack=5:release=200[bgm_duck]");
            fc.append(";[0:a][bgm_duck]amix=inputs=2:duration=first:dropout_transition=2[mixed]");
        } else {
            fc.append(";[a_bgm]anull[mixed]");
        }

        if (fout > 0.0) {
            fc.append(";[mixed]areverse,afade=t=in:st=0:d=").append(fout).append(",areverse[aout]");
        } else {
            fc.append(";[mixed]anull[aout]");
        }

        mix.add("-filter_complex"); mix.add(fc.toString());
        mix.add("-map"); mix.add("0:v");
        mix.add("-map"); mix.add("[aout]");
        mix.add("-c:v"); mix.add("copy");
        mix.add("-c:a"); mix.add("aac");
        mix.add("-shortest");
        mix.add(finalWithBgm.toString());

        ffmpegExecutor.exec(mix.toArray(new String[0]), null);
        MediaIoUtils.safeDelete(finalOut);
        ctx.setFinalOut(finalWithBgm);
    }
}


