package com.zhongjia.subtitlefusion.service.webtoon;

import com.zhongjia.subtitlefusion.model.TaskState;
import com.zhongjia.subtitlefusion.model.WebtoonDramaGenerateRequest;
import com.zhongjia.subtitlefusion.service.DistributedTaskManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 漫剧异步任务：生成草稿 + 可选提交云渲染（只提交，不轮询）。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebtoonDramaAsyncService {

    private final DistributedTaskManagementService tasks;
    private final WebtoonDramaDraftWorkflowService workflow;

    @Async("subtitleTaskExecutor")
    public void processAsync(String taskId, WebtoonDramaGenerateRequest request) {
        try {
            tasks.updateTaskProgress(taskId, TaskState.PROCESSING, 5, "生成漫剧草稿中");

            WebtoonDramaDraftWorkflowService.WebtoonDramaGenResult gen = workflow.generate(request);
            if (gen == null || gen.draftUrl == null || gen.draftUrl.isEmpty()) {
                tasks.markTaskFailed(taskId, "生成草稿失败：draftUrl 为空");
                return;
            }

            // 写 draftUrl 供外部查询
            tasks.updateTaskDraftUrl(taskId, gen.draftUrl);

            if (gen.cloudTaskId != null && !gen.cloudTaskId.isEmpty()) {
                tasks.updateTaskCloudTaskId(taskId, gen.cloudTaskId);
                tasks.updateTaskProgress(taskId, TaskState.PROCESSING, 60, "云渲染任务已提交");
            } else {
                tasks.updateTaskProgress(taskId, TaskState.PROCESSING, 90, "草稿生成完成");
            }

            // 本地任务到此结束：输出 draftUrl（与现有 CapCutDraftAsyncService 行为一致）
            tasks.markTaskCompleted(taskId, gen.draftUrl);
        } catch (Exception e) {
            log.error("[WebtoonDrama] 任务失败 taskId={}", taskId, e);
            tasks.markTaskFailed(taskId, e.getMessage() != null ? e.getMessage() : "生成漫剧草稿失败");
        }
    }
}


