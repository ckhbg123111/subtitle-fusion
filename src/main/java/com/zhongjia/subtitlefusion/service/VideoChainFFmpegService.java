package com.zhongjia.subtitlefusion.service;

import com.zhongjia.subtitlefusion.model.VideoChainRequest;
import com.zhongjia.subtitlefusion.service.videochain.VideoChainContext;
import com.zhongjia.subtitlefusion.service.videochain.VideoChainStep;
import com.zhongjia.subtitlefusion.util.MediaIoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.List;

@Service
@Slf4j
public class VideoChainFFmpegService {

    @Autowired
    private DistributedTaskManagementService tasks;
    @Autowired
    private List<VideoChainStep> steps;

    

    @Async("subtitleTaskExecutor")
    public void processAsync(VideoChainRequest req) {
        VideoChainContext ctx = new VideoChainContext(req.getTaskId(), req);
        try {
            for (VideoChainStep step : steps) {
                long t0 = System.currentTimeMillis();
                if (log.isInfoEnabled()) log.info("Start step: {}", step.name());
                step.execute(ctx);
                long cost = System.currentTimeMillis() - t0;
                if (log.isInfoEnabled()) log.info("End step: {} ({} ms)", step.name(), cost);
            }
        } catch (Exception ex) {
            tasks.markTaskFailed(req.getTaskId(), ex.getMessage());
            if (log.isErrorEnabled()) log.error("Video chain failed on task {}: {}", req.getTaskId(), ex.getMessage(), ex);
        } finally {
            for (Path p : ctx.getTempFiles()) MediaIoUtils.safeDelete(p);
            if (ctx.getFinalOut() != null) MediaIoUtils.safeDelete(ctx.getFinalOut());
        }
    }
}
