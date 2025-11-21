package com.zhongjia.subtitlefusion.service;

import com.zhongjia.subtitlefusion.model.*;
import com.zhongjia.subtitlefusion.model.enums.TextStrategyEnum;
import com.zhongjia.subtitlefusion.model.options.BasicTextOptions;
import com.zhongjia.subtitlefusion.model.options.FlowerTextOptions;
import com.zhongjia.subtitlefusion.model.options.KeywordHighlightOptions;
import com.zhongjia.subtitlefusion.model.options.TextTemplateOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
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
            fulfillDefaultTemplate(request.getSubtitleInfo());
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

    private void fulfillDefaultTemplate(SubtitleInfo subtitleInfo) throws Exception {
        // 对于上游未传模板的情况，填充默认值
        if (subtitleInfo == null) {
            log.info("视频渲染任务缺少字幕");
            return;
        }
        if (subtitleInfo.getSubtitleTemplate() == null) {
            subtitleInfo.setSubtitleTemplate(new SubtitleTemplate());
        }
        SubtitleTemplate subtitleTemplate = subtitleInfo.getSubtitleTemplate();
        if (CollectionUtils.isEmpty(subtitleTemplate.getFlowerTextOptions())) {
            List<FlowerTextOptions> flowerTextOptions = new ArrayList<>();
            FlowerTextOptions flowerTextOption = new FlowerTextOptions();
            flowerTextOption.setEffectId("WklvRVxXQlVNbFpTQVtKakJTVA==");
            flowerTextOptions.add(flowerTextOption);
            subtitleTemplate.setFlowerTextOptions(flowerTextOptions);
        }
        if (CollectionUtils.isEmpty(subtitleTemplate.getTextTemplateOptions())) {
            List<TextTemplateOptions> textTemplateOptions = new ArrayList<>();
            TextTemplateOptions textTemplateOption = new TextTemplateOptions();
            textTemplateOption.setTemplateId("7299286022167285018");
            textTemplateOptions.add(textTemplateOption);
            subtitleTemplate.setTextTemplateOptions(textTemplateOptions);
        }
        if (CollectionUtils.isEmpty(subtitleTemplate.getKeywordHighlightOptions())) {
            List<KeywordHighlightOptions> keywordHighlightOptions = new ArrayList<>();
            KeywordHighlightOptions keywordHighlightOption = new KeywordHighlightOptions();
            keywordHighlightOption.setKeywordsFont("匹喏曹");
            keywordHighlightOption.setKeywordsColor("#FFFF00");
            keywordHighlightOptions.add(keywordHighlightOption);
            subtitleTemplate.setKeywordHighlightOptions(keywordHighlightOptions);
        }
        if (CollectionUtils.isEmpty(subtitleTemplate.getBasicTextOptions())) {
            List<BasicTextOptions> basicTextOptions = new ArrayList<>();
            BasicTextOptions basicTextOption = new BasicTextOptions();
            basicTextOption.setFont("匹喏曹");
            basicTextOption.setFontColor("#FFFFFF");
            basicTextOptions.add(basicTextOption);
            subtitleTemplate.setBasicTextOptions(basicTextOptions);
        }
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
            if (sei.getTextStrategy() != null) continue;

            TextStrategyEnum strategy = decideTextStrategy(si, sei);
            if (strategy != null) {
                sei.setTextStrategy(strategy);
            }
        }
    }

    private TextStrategyEnum decideTextStrategy(SubtitleInfo.CommonSubtitleInfo si, SubtitleInfo.SubtitleEffectInfo sei) {
        if (!CollectionUtils.isEmpty(sei.getKeyWords())) {
            return TextStrategyEnum.KEYWORD;
        }
        if (Boolean.TRUE.equals(sei.getAllowRandomEffect())) {
            // 关键句 花字模板二选一（空文本做保护）
            String text = si.getText();
            int len = text != null ? text.length() : 0;
            boolean preferFlower = ThreadLocalRandom.current().nextBoolean() || len > 6;
            return preferFlower ? TextStrategyEnum.FLOWER : TextStrategyEnum.TEMPLATE;
        }
        return TextStrategyEnum.BASIC;
    }
}


