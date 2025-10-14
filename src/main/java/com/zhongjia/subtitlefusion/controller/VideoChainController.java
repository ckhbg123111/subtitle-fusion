package com.zhongjia.subtitlefusion.controller;

import com.zhongjia.subtitlefusion.model.TaskInfo;
import com.zhongjia.subtitlefusion.model.TaskResponse;
import com.zhongjia.subtitlefusion.model.VideoChainRequest;
import com.zhongjia.subtitlefusion.service.DistributedTaskManagementService;
import com.zhongjia.subtitlefusion.service.VideoChainFFmpegService;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/video-chain")
public class VideoChainController {

    private final DistributedTaskManagementService taskService;
    private final VideoChainFFmpegService ffmpegService;

    public VideoChainController(DistributedTaskManagementService taskService,
                                VideoChainFFmpegService ffmpegService) {
        this.taskService = taskService;
        this.ffmpegService = ffmpegService;
    }

    /**
     * 创建视频链合成任务（异步），立即返回任务信息
     */
    @PostMapping(value = "/tasks", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public TaskResponse createTask(@RequestBody VideoChainRequest request) throws Exception {
        if (request == null || !StringUtils.hasText(request.getTaskId())) {
            return new TaskResponse(null, "taskId 不能为空");
        }
        if (request.getSegmentList() == null || request.getSegmentList().isEmpty()) {
            return new TaskResponse(request.getTaskId(), "segmentList 不能为空");
        }
        if (taskService.taskExists(request.getTaskId())) {
            return new TaskResponse(request.getTaskId(), "任务ID已存在，请更换 taskId");
        }

        TaskInfo taskInfo = taskService.createTask(request.getTaskId());
        ffmpegService.processAsync(request);
        return new TaskResponse(taskInfo);
    }

    /**
     * 查询任务状态
     */
    @GetMapping(value = "/tasks/{taskId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TaskResponse getTask(@PathVariable("taskId") String taskId) {
        TaskInfo taskInfo = taskService.getTask(taskId);
        if (taskInfo == null) {
            return new TaskResponse(taskId, "任务不存在");
        }
        return new TaskResponse(taskInfo);
    }
}


