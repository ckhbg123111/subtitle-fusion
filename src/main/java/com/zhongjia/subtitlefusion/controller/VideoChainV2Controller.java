package com.zhongjia.subtitlefusion.controller;

import com.zhongjia.subtitlefusion.model.TaskInfo;
import com.zhongjia.subtitlefusion.model.TaskResponse;
import com.zhongjia.subtitlefusion.model.VideoChainV2Request;
import com.zhongjia.subtitlefusion.model.Result;
import com.zhongjia.subtitlefusion.model.CapCutCloudTaskStatus;
import com.zhongjia.subtitlefusion.service.DistributedTaskManagementService;
import com.zhongjia.subtitlefusion.service.videochainv2.VideoChainV2AsyncService;
import com.zhongjia.subtitlefusion.service.api.CapCutApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/video-chain-v2")
public class VideoChainV2Controller {

    @Autowired
    private DistributedTaskManagementService taskService;
    @Autowired
    private VideoChainV2AsyncService asyncService;
    @Autowired
    private CapCutApiClient apiClient;

    /**
     * 创建视频链合成任务（异步），立即返回任务信息
     */
    @PostMapping(value = "/tasks", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public TaskResponse createTask(@RequestBody VideoChainV2Request request) throws Exception {
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
        asyncService.processAsync(request.getTaskId(), request);
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

    /**
     * 触发云渲染，返回 cloudTaskId
     */
    @PostMapping(value = "/cloud-render", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Result<String> cloudRender(@RequestBody CloudRenderRequest req) {
        if (req == null || !StringUtils.hasText(req.getDraftId())) {
            return Result.error("draftId 不能为空");
        }
        try {
            com.zhongjia.subtitlefusion.model.capcut.CapCutResponse<com.zhongjia.subtitlefusion.model.capcut.GenerateVideoOutput> resp =
                    apiClient.generateVideo(req.getDraftId(), req.getResolution(), req.getFramerate());
            if (resp != null && Boolean.TRUE.equals(resp.getSuccess()) && resp.getOutput() != null) {
                return Result.success(resp.getOutput().getTaskId());
            }
            String err = resp != null && resp.getError() != null ? resp.getError() : "触发云渲染失败";
            return Result.error(err);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 云渲染任务进度查询
     */
    @GetMapping(value = "/cloud-task/{cloudTaskId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result<CapCutCloudTaskStatus> getCloudTaskStatus(@PathVariable String cloudTaskId) {
        if (!StringUtils.hasText(cloudTaskId)) {
            return Result.error("cloudTaskId 不能为空");
        }
        com.zhongjia.subtitlefusion.model.CapCutCloudResponse<CapCutCloudTaskStatus> resp = apiClient.taskStatus(cloudTaskId);
        if (resp != null && Boolean.TRUE.equals(resp.getSuccess())) {
            return Result.success(resp.getOutput());
        }
        String err = (resp != null && resp.getError() != null) ? resp.getError() : "查询失败";
        return Result.error(err);
    }

    public static class CloudRenderRequest {
        private String draftId;
        private String resolution;
        private String framerate;
        public String getDraftId() { return draftId; }
        public void setDraftId(String draftId) { this.draftId = draftId; }
        public String getResolution() { return resolution; }
        public void setResolution(String resolution) { this.resolution = resolution; }
        public String getFramerate() { return framerate; }
        public void setFramerate(String framerate) { this.framerate = framerate; }
    }
}