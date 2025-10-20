package com.zhongjia.subtitlefusion.service.videochain;

import com.zhongjia.subtitlefusion.ffmpeg.FFmpegExecutor;
import com.zhongjia.subtitlefusion.ffmpeg.transition.TransitionStrategy;
import com.zhongjia.subtitlefusion.ffmpeg.transition.TransitionStrategyFactory;
import com.zhongjia.subtitlefusion.model.TaskState;
import com.zhongjia.subtitlefusion.service.DistributedTaskManagementService;
import com.zhongjia.subtitlefusion.util.MediaIoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

@Component
@Order(30)
public class ConcatSegmentsStep implements VideoChainStep {

    @Autowired
    private DistributedTaskManagementService tasks;
    @Autowired
    private FFmpegExecutor ffmpegExecutor;

    @Override
    public String name() {
        return "ConcatSegments";
    }

    @Override
    public void execute(VideoChainContext ctx) throws Exception {
        String taskId = ctx.getTaskId();
        tasks.updateTaskProgress(taskId, TaskState.PROCESSING, 70, "段间拼接");

        Path workDir = ctx.getWorkDir();
        List<Path> segmentOutputs = ctx.getSegmentOutputs();

        Path concatList = workDir.resolve("concat_all.txt");
        MediaIoUtils.writeConcatList(concatList, segmentOutputs);
        ctx.getTempFiles().add(concatList);

        Path finalOut = workDir.resolve("final_" + taskId + ".mp4");
        // 仅当请求中提供了 transition 枚举时启用转场；否则关闭
        if (ctx.getRequest().getTransition() != null) {
            String type = toStrategyName(ctx.getRequest().getTransition());
            double dur = 0.5; // 硬编码转场时长（秒）
            TransitionStrategy strategy = TransitionStrategyFactory.get(type);
            String[] cmd = strategy.buildCommand(segmentOutputs, dur, finalOut);
            ffmpegExecutor.exec(cmd, null);
        } else {
            ffmpegExecutor.exec(new String[]{
                    "ffmpeg", "-y",
                    "-f", "concat", "-safe", "0",
                    "-i", concatList.toString(),
                    "-c", "copy",
                    finalOut.toString()
            }, null);
        }

        tasks.updateTaskProgress(taskId, TaskState.PROCESSING, 85, "拼接完成");
        ctx.setFinalOut(finalOut);
    }

    private String toStrategyName(com.zhongjia.subtitlefusion.model.VideoChainRequest.TransitionType t) {
        // 与工厂注册名称对齐（小写字符串）
        switch (t) {
            case ZOOM: return "zoom";
            case FADE: return "fade";
            case DISSOLVE: return "dissolve";
            case WIPELEFT: return "wipeleft";
            case WIPERIGHT: return "wiperight";
            case WIPEUP: return "wipeup";
            case WIPEDOWN: return "wipedown";
            case SMOOTHLEFT: return "smoothleft";
            case SMOOTHRIGHT: return "smoothright";
            case SMOOTHUP: return "smoothup";
            case SMOOTHDOWN: return "smoothdown";
            case CIRCLEOPEN: return "circleopen";
            case CIRCLECLOSE: return "circleclose";
            case RADIAL: return "radial";
            case ZOOMIN: return "zoomin";
            case FADEBLACK: return "fadeblack";
            case FADEWHITE: return "fadewhite";
            case DISTANCE: return "distance";
            case HLSLICE: return "hlslice";
            case VLSLICE: return "vlslice";
            case PIXELIZE: return "pixelize";
            case RIPPLE: return "ripple";
            case SQUEEZEH: return "squeezeh";
            case SQUEEZEV: return "squeezev";
            default: return "zoom";
        }
    }
}


