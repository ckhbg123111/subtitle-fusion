package com.zhongjia.subtitlefusion.service;

import com.zhongjia.subtitlefusion.model.CapCutGenResponse;
import com.zhongjia.subtitlefusion.model.SubtitleFusionV2Request;
import com.zhongjia.subtitlefusion.model.TaskState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
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
        applyTemporaryEffectFallback(request);

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

    // ===== 临时逻辑：当没有关键词时，随机补充花字或文字模板（可随时移除） =====
    private static final List<String> TEMP_FLOWER_TEXT_EFFECT_IDS = Arrays.asList(
            // 花字效果ID（仅在线接口支持）。来源：search_artist & 提供的对照表。
            "W0FmRVRXQV1EZ1JRS11BbEBWVQ==", // 金色金属质感立体花字
            "WkloR1RQSlZGb1xRSFxNbkRcVA==", // 复古立体黄色花字
            "WkpuQldVQlBDZlJcS19LaENcUg==", // 运动教程-花字
            "WkhtRF1QQlNBZllSTFlMZktSUg==", // 综艺 白色
            "W0BpSlRRRldCZlhQTFpAaERcUw==", // 黄色花字
            "WkprRFxVRVxEaV1TQFlIakRUVQ==", // 系统故障字
            "WktpQ1ZXQldAZl5dSFxLZ0pcVQ==", // 黑白黄色花字
            "WktuQV1aQFNEallQQF1LakRXVg==", // 美食-划痕
            "W0BuQldSQFZCbllUSVVJZkVVVA==", // 潮酷金黄色发光霓虹灯牌花字
            "WkpuRFxRQlBNalpSS19IaUNSVg==", // 知识-花字
            "WkpvR1xSQFxEZ19cQF1PbUNQVg==", // 浅草绿色二层简约花字
            "W0BuSl1XR1xGbFtTTFhIakFSVA==", // 字体
            "W0BpRl1WRFRHZl9VQFRPZ0BcUg==", // 白边荧光绿纹理花字
            "WkpuRFxSRlFGbFpdT1VLb0BcUg==", // 知识-花字
            "WklvS1ZXR1JHaVJXTVlIaEFcVw==", // 综艺 白色
            "WklvRlFXQ1BNZ1hdQFhOb0tXWg==", // 土酷橘黄黑色化学公式背景字体
            "W0BpQFRaQ1NMbVxUS19MZktSVQ==", // 金色纹理花字
            "W0FmRVRQSlRGbVxSTVlAZ0ZTVA==", // 土酷黄色花字
            "W0BoRVxQQF1Da1JcSlVAaURUUQ==", // 白色发光金光黄光立体花字
            "W0BtRFRVQlRAa19XSFpBa0tWUQ==", // 简约黑色描边立体花字
            "WktrQVNSR1FDaFJXQFVObUVcVA==", // 小清新绿色描边花字
            "WklsRVNRQFFEa1JcSV5JaEVUVw==", // 白字描边
            "W0BmQFNaQVJBbFlRTVlLbkBdUA==", // 红色花字
            "W0BpQFxRQlBAb19VS1VKb0VXUQ==", // 金色发光纹理花字
            "WkhtRFNRQ1FHa1lWSV9PbENSVQ==", // 小清新 白色蓝边
            "W0BoRFZVQ11NbVtQTVlMbEZWVg=="  // 绿色（渐变）花字
    );

    private static final List<String> TEMP_TEXT_TEMPLATE_IDS = Arrays.asList(
            // 文字模板ID（来自提供的对照表）
            "7163524521452948744",
            "7296096429938904339",
            "7296371914266692915",
            "7299286022167285018",
            "7311588013618908451",
            "7351211478738849035",
            "7359462259493539108",
            "7362412232107511090",
            "7371110731258858779",
            "7371340923453721883",
            "7372502722764999990",
            "7372506200237264191",
            "7373557927950437658",
            "7383613902744980770",
            "7384783886825393462",
            "7392152029893856551",
            "7393008717316279604",
            "7393022390638251303"
    );

    private void applyTemporaryEffectFallback(SubtitleFusionV2Request request) {
        if (request == null || request.getSubtitleInfo() == null || request.getSubtitleInfo().getCommonSubtitleInfoList() == null) {
            return;
        }
        List<SubtitleFusionV2Request.CommonSubtitleInfo> items = request.getSubtitleInfo().getCommonSubtitleInfoList();
        for (SubtitleFusionV2Request.CommonSubtitleInfo si : items) {
            if (si == null) continue;
            SubtitleFusionV2Request.SubtitleEffectInfo sei = si.getSubtitleEffectInfo();
            if (sei == null) {
                sei = new SubtitleFusionV2Request.SubtitleEffectInfo();
                si.setSubtitleEffectInfo(sei);
            }

            boolean hasKeywords = sei.getKeyWords() != null && !sei.getKeyWords().isEmpty();
            boolean hasFlower = sei.getTextEffectId() != null && !sei.getTextEffectId().isEmpty();
            boolean hasTemplate = sei.getTextTemplateId() != null && !sei.getTextTemplateId().isEmpty();
            if (hasKeywords || hasFlower || hasTemplate) {
                continue;
            }

            // 随机二选一：花字 or 模板；若花字列表为空则回退到模板
            boolean preferFlower = java.util.concurrent.ThreadLocalRandom.current().nextBoolean();
            String chosenFlower = chooseRandom(TEMP_FLOWER_TEXT_EFFECT_IDS);
            String chosenTemplate = chooseRandom(TEMP_TEXT_TEMPLATE_IDS);

            if (preferFlower && chosenFlower != null) {
                sei.setTextEffectId(chosenFlower);
            } else if (chosenTemplate != null) {
                sei.setTextTemplateId(chosenTemplate);
            } else if (chosenFlower != null) {
                // 模板不可用时兜底为花字
                sei.setTextEffectId(chosenFlower);
            }
        }
    }

    private static <T> T chooseRandom(List<T> list) {
        if (list == null || list.isEmpty()) return null;
        int idx = java.util.concurrent.ThreadLocalRandom.current().nextInt(list.size());
        return list.get(idx);
    }
}


