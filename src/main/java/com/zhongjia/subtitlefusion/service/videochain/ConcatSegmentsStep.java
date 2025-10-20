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
            double dur = 1; // 硬编码转场时长（秒）
            TransitionStrategy strategy = TransitionStrategyFactory.getStrategy("xfade");
            String[] cmd = strategy.buildCommand(segmentOutputs, dur, type, finalOut);
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

    /*
     * 扩展规划（转场）：
     * 1) 多策略并存：当前仅使用 xfade（通过枚举映射到具体 transition 名称），
     *    后续可新增非 xfade 实现（如自定义缩放：zoompan/scale + blend/overlay），
     *    以及 GPU/硬编方案（CUDA/VAAPI）作为独立策略注册到工厂。
     * 2) 能力探测与降级：启动或首次调用时探测 ffmpeg 支持的 transition；
     *    若请求的 transition 不可用，自动回退到“zoomin”或“fade”。
     * 3) 音频差异化处理：
     *    - 目前对无音轨段注入静音并使用 acrossfade；
     *    - 可扩展为可配置策略（纯截断/淡出到静音/ducking）。
     * 4) 可测试性：保留策略与命令构造分层，单元测试校验 filter_complex 生成结果，
     *    在不同 OS/容器构建上保证一致行为。
     * 5) 参数化：本类仅做枚举到名称的映射，参数（时长、曲线等）由策略层统一收口，
     *    未来如需不同转场的特定参数（例如像素化块大小、滑动方向），在策略实现中拓展。
     */
    private String toStrategyName(com.zhongjia.subtitlefusion.model.VideoChainRequest.TransitionType t) {
        // 与工厂注册名称对齐（小写字符串）
        switch (t) {
            case CUSTOM: return "custom";
            case FADE: return "fade";
            case DISSOLVE: return "dissolve";
            case WIPELEFT: return "wipeleft";
            case WIPERIGHT: return "wiperight";
            case WIPEUP: return "wipeup";
            case WIPEDOWN: return "wipedown";
            case SLIDELEFT: return "slideleft";
            case SLIDERIGHT: return "slideright";
            case SLIDEUP: return "slideup";
            case SLIDEDOWN: return "slidedown";
            case CIRCLECROP: return "circlecrop";
            case RECTCROP: return "rectcrop";
            case SMOOTHLEFT: return "smoothleft";
            case SMOOTHRIGHT: return "smoothright";
            case SMOOTHUP: return "smoothup";
            case SMOOTHDOWN: return "smoothdown";
            case CIRCLEOPEN: return "circleopen";
            case CIRCLECLOSE: return "circleclose";
            case RADIAL: return "radial";
            case FADEBLACK: return "fadeblack";
            case FADEWHITE: return "fadewhite";
            case DISTANCE: return "distance";
            case HLSLICE: return "hlslice";
            case HRSLICE: return "hrslice";
            case VUSLICE: return "vuslice";
            case VDSLICE: return "vdslice";
            case HBLUR: return "hblur";
            case FADEGRAYS: return "fadegrays";
            case WIPETL: return "wipetl";
            case WIPETR: return "wipetr";
            case WIPEBL: return "wipebl";
            case WIPEBR: return "wipebr";
            case PIXELIZE: return "pixelize";
            case SQUEEZEH: return "squeezeh";
            case SQUEEZEV: return "squeezev";
            case ZOOMIN: return "zoomin";
            case FADEFAST: return "fadefast";
            case FADESLOW: return "fadeslow";
            case HLWIND: return "hlwind";
            case HRWIND: return "hrwind";
            case VUWIND: return "vuwind";
            case VDWIND: return "vdwind";
            case COVERLEFT: return "coverleft";
            case COVERRIGHT: return "coverright";
            case COVERUP: return "coverup";
            case COVERDOWN: return "coverdown";
            case REVEALLEFT: return "revealleft";
            case REVEALRIGHT: return "revealright";
            case REVEALUP: return "revealup";
            case REVEALDOWN: return "revealdown";
            default: return "zoomin"; // 兜底
        }
    }
}


