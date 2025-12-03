package com.zhongjia.subtitlefusion.controller;

import com.zhongjia.subtitlefusion.model.TaskInfo;
import com.zhongjia.subtitlefusion.model.TaskResponse;
import com.zhongjia.subtitlefusion.service.DistributedTaskManagementService;
import com.zhongjia.subtitlefusion.service.TemporaryCloudRenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/temp-cloud-render")
@RequiredArgsConstructor
@Slf4j
public class TemporaryCloudRenderController {

    private final DistributedTaskManagementService taskService;
    private final TemporaryCloudRenderService cloudRenderService;

    /**
     * 创建临时云渲染任务
     */
    @PostMapping(value = "/tasks", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public TaskResponse createTask(@RequestBody CloudRenderTaskCreateRequest request) throws Exception {
        if (request == null || !StringUtils.hasText(request.getDraftId())) {
            return new TaskResponse(null, "draftId 不能为空");
        }

        String taskId = request.getTaskId();
        if (!StringUtils.hasText(taskId)) {
            taskId = "temp-cloud-" + UUID.randomUUID().toString().replace("-", "");
        }

        if (taskService.taskExists(taskId)) {
            return new TaskResponse(taskId, "任务ID已存在，请更换 taskId");
        }

        TaskInfo taskInfo = taskService.createTask(taskId);
        // 异步执行：提交云渲染 + 轮询 + 下载&上传 MinIO
        cloudRenderService.processCloudRenderAsync(taskId, request.getDraftId(), request.getResolution(), request.getFramerate());
        return new TaskResponse(taskInfo);
    }

    /**
     * 查询临时云渲染任务状态
     */
    @GetMapping(value = "/tasks/{taskId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TaskResponse getTask(@PathVariable("taskId") String taskId) {
        TaskInfo taskInfo = taskService.getTask(taskId);
        if (taskInfo == null) {
            return new TaskResponse(taskId, "任务不存在");
        }
        return new TaskResponse(taskInfo);
    }

    /**
     * 临时云渲染任务创建请求体
     */
    public static class CloudRenderTaskCreateRequest {
        private String draftId;
        private String resolution;
        private String framerate;
        private String taskId;

        public String getDraftId() {
            return draftId;
        }

        public void setDraftId(String draftId) {
            this.draftId = draftId;
        }

        public String getResolution() {
            return resolution;
        }

        public void setResolution(String resolution) {
            this.resolution = resolution;
        }

        public String getFramerate() {
            return framerate;
        }

        public void setFramerate(String framerate) {
            this.framerate = framerate;
        }

        public String getTaskId() {
            return taskId;
        }

        public void setTaskId(String taskId) {
            this.taskId = taskId;
        }
    }
}


