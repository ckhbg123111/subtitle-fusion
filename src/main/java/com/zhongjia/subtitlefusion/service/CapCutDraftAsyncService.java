package com.zhongjia.subtitlefusion.service;

import com.zhongjia.subtitlefusion.model.CapCutGenResponse;
import com.zhongjia.subtitlefusion.model.SubtitleFusionV2Request;
import com.zhongjia.subtitlefusion.model.TaskState;
import com.zhongjia.subtitlefusion.service.api.CapCutApiClient;
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
    private final CapCutApiClient apiClient;

    @Async("subtitleTaskExecutor")
    public CompletableFuture<Void> processAsync(String taskId, SubtitleFusionV2Request request) {
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

            // 云渲染：轮询远端任务直至完成
            String cloudTaskId = gen.getTaskId();
            if (cloudTaskId == null || cloudTaskId.isEmpty()) {
                tasks.markTaskFailed(taskId, "云渲染任务提交失败：taskId 为空");
                return CompletableFuture.completedFuture(null);
            }

            tasks.updateTaskProgress(taskId, TaskState.PROCESSING, 40, "云渲染任务已提交：" + cloudTaskId);

            int safeProgressBase = 45; // 轮询时的下限进度
            int safeProgressMax = 95;  // 完成前的上限进度
            long start = System.currentTimeMillis();
            long timeoutMs = 60L * 60L * 1000L; // 最长轮询1小时，按需调整

            while (System.currentTimeMillis() - start < timeoutMs) {
                try {
                    com.zhongjia.subtitlefusion.model.CapCutCloudTaskStatus s = apiClient.taskStatus(cloudTaskId);
                    Integer p = s.getProgress();
                    String msg = s.getMessage();
                    String status = s.getStatus();
                    boolean ok = s.isSuccess();
                    if (p == null) p = 0;
                    int bounded = Math.max(safeProgressBase, Math.min(safeProgressMax, p));
                    tasks.updateTaskProgress(taskId, TaskState.PROCESSING, bounded, msg != null ? msg : ("状态：" + status));

                    if (ok && s.getResultUrl() != null && !s.getResultUrl().isEmpty()) {
                        tasks.updateTaskProgress(taskId, TaskState.PROCESSING, 98, "云渲染成功，写入结果");
                        tasks.markTaskCompleted(taskId, s.getResultUrl());
                        return CompletableFuture.completedFuture(null);
                    }

                    if ("FAILURE".equalsIgnoreCase(status)) {
                        String err = s.getError() != null ? s.getError() : (msg != null ? msg : "云渲染失败");
                        tasks.markTaskFailed(taskId, err);
                        return CompletableFuture.completedFuture(null);
                    }
                } catch (Exception pollEx) {
                    log.warn("[CapCutAsync] 轮询失败: {}", pollEx.getMessage());
                }

                try { Thread.sleep(3000L); } catch (InterruptedException ignore) { Thread.currentThread().interrupt(); }
            }

            tasks.markTaskFailed(taskId, "云渲染轮询超时");
        } catch (Exception e) {
            tasks.markTaskFailed(taskId, e.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }
}


