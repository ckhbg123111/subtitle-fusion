package com.zhongjia.subtitlefusion.controller;

import com.zhongjia.subtitlefusion.model.ScriptDrivenSegmentRequest;
import com.zhongjia.subtitlefusion.model.TaskInfo;
import com.zhongjia.subtitlefusion.model.TaskResponse;
import com.zhongjia.subtitlefusion.service.DistributedTaskManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/script-driven")
public class ScriptDrivenController {

    @Autowired
    private DistributedTaskManagementService taskService;

    /**
     * 提交脚本驱动分段请求（根为数组），创建任务并返回唯一任务ID
     */
    @PostMapping(value = "/tasks", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public TaskResponse submit(@RequestBody List<ScriptDrivenSegmentRequest> requests) throws Exception {
        if (requests == null || requests.isEmpty()) {
            return new TaskResponse(null, "请求体不能为空，至少需要一条记录");
        }

        String taskId = generateUniqueTaskId();
        TaskInfo taskInfo = taskService.createTask(taskId);
        return new TaskResponse(taskInfo);
    }

    private String generateUniqueTaskId() {
        // 生成不重复的任务ID（带前缀），冲突时重试
        for (int i = 0; i < 5; i++) {
            String id = "SD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
            if (!taskService.taskExists(id)) {
                return id;
            }
        }
        // 极低概率：多次冲突则退化为纯UUID
        String fallback = UUID.randomUUID().toString();
        if (taskService.taskExists(fallback)) {
            // 最后兜底再拼时间戳，确保不与现有任务冲突
            fallback = fallback + "-" + System.currentTimeMillis();
        }
        return fallback;
    }
}


