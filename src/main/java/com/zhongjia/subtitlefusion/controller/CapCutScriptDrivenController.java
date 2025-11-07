package com.zhongjia.subtitlefusion.controller;

import com.zhongjia.subtitlefusion.model.SubtitleFusionV2Request;
import com.zhongjia.subtitlefusion.model.TaskInfo;
import com.zhongjia.subtitlefusion.model.TaskResponse;
import com.zhongjia.subtitlefusion.service.CapCutDraftAsyncService;
import com.zhongjia.subtitlefusion.service.DistributedTaskManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
 

@RestController
@RequestMapping("/api/capcut-script-driven")
@Slf4j
@RequiredArgsConstructor
public class CapCutScriptDrivenController {

    private final DistributedTaskManagementService taskService;
    private final CapCutDraftAsyncService asyncService;

    @PostMapping(value = "/capcut-gen", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public TaskResponse submit(@RequestBody SubtitleFusionV2Request request)  {
        String taskId = request.getTaskId();
        String videoUrl = request.getVideoUrl();
        if (!StringUtils.hasText(taskId)) {
            return new TaskResponse(null, "taskId 不能为空");
        }
        if (!StringUtils.hasText(videoUrl)) {
            return new TaskResponse(taskId, "videoUrl 不能为空");
        }
        if (taskService.taskExists(taskId)) {
            return new TaskResponse(taskId, "任务ID已存在，请使用不同的taskId");
        }
        try {
            TaskInfo taskInfo = taskService.createTask(taskId);
            asyncService.processAsync(taskId, request);
            return new TaskResponse(taskInfo);
        } catch (Exception e) {
            return new TaskResponse(taskId, e.getMessage());
        }
    }

    @GetMapping(value = "/task/{taskId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TaskResponse getTaskStatus(@PathVariable String taskId) {
        TaskInfo taskInfo = taskService.getTask(taskId);
        if (taskInfo == null) {
            return new TaskResponse(taskId, "任务不存在");
        }
        return new TaskResponse(taskInfo);
    }


}
