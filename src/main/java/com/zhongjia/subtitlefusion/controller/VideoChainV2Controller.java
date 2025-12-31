package com.zhongjia.subtitlefusion.controller;

import com.zhongjia.subtitlefusion.model.*;
import com.zhongjia.subtitlefusion.model.enums.ErrorCode;
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
    public Result<TaskResponse> createTask(@RequestBody VideoChainV2Request request) throws Exception {
        if (request == null || !StringUtils.hasText(request.getTaskId())) {
            return Result.error(ErrorCode.BAD_REQUEST, "taskId 不能为空");
        }
        if (request.getSegmentList() == null || request.getSegmentList().isEmpty()) {
            return Result.error(ErrorCode.BAD_REQUEST, "segmentList 不能为空");
        }
        // 段落主素材：videoInfos 与 keyframeInfo 二选一
        for (int i = 0; i < request.getSegmentList().size(); i++) {
            VideoChainV2Request.SegmentInfo seg = request.getSegmentList().get(i);
            boolean hasVideo = false;
            if (seg != null && seg.getVideoInfos() != null) {
                for (VideoChainV2Request.VideoInfo vi : seg.getVideoInfos()) {
                    if (vi != null && StringUtils.hasText(vi.getVideoUrl())) {
                        hasVideo = true;
                        break;
                    }
                }
            }
            boolean hasKeyframeUrl = seg != null
                    && seg.getKeyframeInfo() != null
                    && StringUtils.hasText(seg.getKeyframeInfo().getPictureUrl());

            if (hasVideo && hasKeyframeUrl) {
                return Result.error(ErrorCode.BAD_REQUEST, "第 " + (i + 1) + " 段 videoInfos 与 keyframeInfo.pictureUrl 不能同时存在");
            }
            if (!hasVideo && !hasKeyframeUrl) {
                return Result.error(ErrorCode.BAD_REQUEST, "第 " + (i + 1) + " 段缺少主素材：videoInfos 或 keyframeInfo.pictureUrl 至少提供一个");
            }
        }
        if (taskService.taskExists(request.getTaskId())) {
            return Result.error(ErrorCode.BAD_REQUEST, "任务ID已存在，请更换 taskId");
        }

        TaskInfo taskInfo = taskService.createTask(request.getTaskId());
        asyncService.processAsync(request.getTaskId(), request, true);
        return Result.success(new TaskResponse(taskInfo));
    }

    /**
     * 查询任务状态
     */
    @GetMapping(value = "/tasks/{taskId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result<TaskResponse> getTask(@PathVariable("taskId") String taskId) {
        TaskInfo taskInfo = taskService.getTask(taskId);
        if (taskInfo == null) {
            return Result.error("任务不存在");
        }
        return Result.success(new TaskResponse(taskInfo));
    }

    /**
     * 云渲染任务进度查询
     */
    @GetMapping(value = "/cloud-task/{cloudTaskId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result<CapCutCloudTaskStatus> getCloudTaskStatus(@PathVariable String cloudTaskId) {
        if (!StringUtils.hasText(cloudTaskId)) {
            return Result.error("cloudTaskId 不能为空");
        }
        CapCutCloudResponse<CapCutCloudTaskStatus> resp = apiClient.taskStatus(cloudTaskId);
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