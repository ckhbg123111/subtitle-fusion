package com.zhongjia.subtitlefusion.service;

import com.zhongjia.subtitlefusion.model.CapCutGenResponse;
import com.zhongjia.subtitlefusion.model.SubtitleFusionV2Request;
import com.zhongjia.subtitlefusion.model.TaskState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class CapCutDraftAsyncService {

    private final DistributedTaskManagementService tasks;
    private final DraftWorkflowService draftWorkflowService;

    @Async("subtitleTaskExecutor")
    public CompletableFuture<Void> processAsync(String taskId, SubtitleFusionV2Request request) {
        // 调用端暂时不传输花字效果和文字模板
        // todo 当没有keywords时，随机选择一个花字或者选择一个文字模板，花字和文字模板见MCP接口文档

        try {
            tasks.updateTaskProgress(taskId, TaskState.PROCESSING, 10, "生成草稿中");
            CapCutGenResponse gen = draftWorkflowService.generateDraft(request);

            if (!gen.isSuccess()) {
                tasks.markTaskFailed(taskId, gen.getMessage() != null ? gen.getMessage() : "生成草稿失败");
                return CompletableFuture.completedFuture(null);
            }

            boolean cloud = Boolean.TRUE.equals(gen.getCloudRendering());
            if (!cloud) {
                tasks.updateTaskProgress(taskId, TaskState.PROCESSING, 90, "草稿保存完成");
                tasks.markTaskCompleted(taskId, gen.getDraftUrl());
                return CompletableFuture.completedFuture(null);
            }

            // 云渲染：仅提交，不轮询
            String cloudTaskId = gen.getTaskId();
            if (cloudTaskId == null || cloudTaskId.isEmpty()) {
                tasks.markTaskFailed(taskId, "云渲染任务提交失败：taskId 为空");
                return CompletableFuture.completedFuture(null);
            }

            tasks.updateTaskProgress(taskId, TaskState.PROCESSING, 60, "云渲染任务已提交");
            // 将 cloudTaskId 存入任务，便于调用方后续查询云渲染进度
            com.zhongjia.subtitlefusion.model.TaskInfo ti = tasks.getTask(taskId);
            if (ti != null) {
                ti.setCloudTaskId(cloudTaskId);
            }
            // 本地任务只负责“提交”，到此即可完成
            tasks.markTaskCompleted(taskId, gen.getDraftUrl());
        } catch (Exception e) {
            tasks.markTaskFailed(taskId, e.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }
}


