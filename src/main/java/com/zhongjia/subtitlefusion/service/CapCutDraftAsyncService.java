package com.zhongjia.subtitlefusion.service;

import com.zhongjia.subtitlefusion.model.*;
import com.zhongjia.subtitlefusion.model.enums.TextStrategyEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class CapCutDraftAsyncService {

    private final DistributedTaskManagementService tasks;
    private final DraftWorkflowService draftWorkflowService;

    @Async("subtitleTaskExecutor")
    public CompletableFuture<Void> processAsync(String taskId, SubtitleFusionV2Request request) {

        try {
            applyTemporaryEffectFallback(request);
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
            TaskInfo ti = tasks.getTask(taskId);
            if (ti != null) {
                ti.setCloudTaskId(cloudTaskId);
            }
            // 本地任务只负责“提交”，到此即可完成
            tasks.markTaskCompleted(taskId, gen.getDraftUrl());
        } catch (Exception e) {
            log.error("生成草稿失败, taskId={}", taskId, e);
            tasks.markTaskFailed(taskId, e.getMessage() != null ? e.getMessage() : "生成草稿失败");
        }
        return CompletableFuture.completedFuture(null);
    }

    private void applyTemporaryEffectFallback(SubtitleFusionV2Request request) {
        if (request == null || request.getSubtitleInfo() == null || request.getSubtitleInfo().getCommonSubtitleInfoList() == null) {
            return;
        }
        List<SubtitleInfo.CommonSubtitleInfo> items = request.getSubtitleInfo().getCommonSubtitleInfoList();
        for (SubtitleInfo.CommonSubtitleInfo si : items) {
            if (si == null) continue;
            SubtitleInfo.SubtitleEffectInfo sei = si.getSubtitleEffectInfo();
            if (sei == null) {
                sei = new SubtitleInfo.SubtitleEffectInfo();
                si.setSubtitleEffectInfo(sei);
            }

            if(sei.getTextStrategy()!=null){
                continue;
            }
            boolean hasKeywords = !CollectionUtils.isEmpty(sei.getKeyWords());
            if (hasKeywords) {
                sei.setTextStrategy(TextStrategyEnum.KEYWORD);
                continue;
            }

            if(Boolean.FALSE.equals(sei.getAllowRandomEffect())){
                sei.setTextStrategy(TextStrategyEnum.BASIC);
                continue;
            }
            // 关键句 花字模板二选一
            boolean preferFlower = ThreadLocalRandom.current().nextBoolean() || si.getText().length() > 6;
            if(preferFlower){
                sei.setTextStrategy(TextStrategyEnum.FLOWER);
            }else{
                sei.setTextStrategy(TextStrategyEnum.TEMPLATE);
            }
        }
    }
}


