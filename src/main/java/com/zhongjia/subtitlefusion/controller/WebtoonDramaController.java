package com.zhongjia.subtitlefusion.controller;

import com.zhongjia.subtitlefusion.model.Result;
import com.zhongjia.subtitlefusion.model.TaskInfo;
import com.zhongjia.subtitlefusion.model.TaskResponse;
import com.zhongjia.subtitlefusion.model.WebtoonDramaGenerateRequest;
import com.zhongjia.subtitlefusion.model.enums.ErrorCode;
import com.zhongjia.subtitlefusion.service.DistributedTaskManagementService;
import com.zhongjia.subtitlefusion.service.webtoon.WebtoonDramaAsyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/webtoon-drama")
@RequiredArgsConstructor
public class WebtoonDramaController {

    private final DistributedTaskManagementService taskService;
    private final WebtoonDramaAsyncService asyncService;

    /**
     * 漫剧草稿生成任务创建（可选提交云渲染，仅提交不轮询）。
     */
    @PostMapping(value = "/tasks", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Result<TaskResponse> createTask(@RequestBody WebtoonDramaGenerateRequest request) throws Exception {
        if (request == null || CollectionUtils.isEmpty(request.getSegment())) {
            return Result.error(ErrorCode.BAD_REQUEST, "segment 不能为空");
        }

        String taskId = "webtoon-" + UUID.randomUUID().toString().replace("-", "");
        // 极低概率冲突：简单重试
        int retry = 0;
        while (taskService.taskExists(taskId) && retry++ < 3) {
            taskId = "webtoon-" + UUID.randomUUID().toString().replace("-", "");
        }
        if (taskService.taskExists(taskId)) {
            return Result.error(ErrorCode.BAD_REQUEST, "任务ID冲突，请重试");
        }

        TaskInfo taskInfo = taskService.createTask(taskId);
        asyncService.processAsync(taskId, request);
        return Result.success(new TaskResponse(taskInfo));
    }
}


