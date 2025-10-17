package com.zhongjia.subtitlefusion.service.videochain;

import com.zhongjia.subtitlefusion.ffmpeg.FFmpegExecutor;
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
        ffmpegExecutor.exec(new String[]{
                "ffmpeg", "-y",
                "-f", "concat", "-safe", "0",
                "-i", concatList.toString(),
                "-c", "copy",
                finalOut.toString()
        }, null);

        tasks.updateTaskProgress(taskId, TaskState.PROCESSING, 85, "拼接完成");
        ctx.setFinalOut(finalOut);
    }
}


