package com.zhongjia.subtitlefusion.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务响应
 */
@Data
public class TaskResponse {
    private String taskId;
    private TaskState state;
    private String message;
    private String outputUrl;
    // 草稿下载地址（如果已生成）
    private String draftUrl;
    // 新增素材资源压缩包
    private String resourcePackageZipUrl;
    private String errorMessage;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private int progress;
    // 新增：云渲染任务ID（若 cloudRendering=true 会返回）
    private String cloudTaskId;

    public TaskResponse() {}

    public TaskResponse(TaskInfo taskInfo) {
        this.taskId = taskInfo.getTaskId();
        this.state = taskInfo.getState();
        this.message = taskInfo.getMessage();
        this.outputUrl = taskInfo.getOutputUrl();
        this.draftUrl = taskInfo.getDraftUrl();
        this.resourcePackageZipUrl = taskInfo.getResourcePackageZipUrl();
        this.errorMessage = taskInfo.getErrorMessage();
        this.createTime = taskInfo.getCreateTime();
        this.updateTime = taskInfo.getUpdateTime();
        this.progress = taskInfo.getProgress();
        this.cloudTaskId = taskInfo.getCloudTaskId();
    }

    public TaskResponse(String taskId, String message) {
        this.taskId = taskId;
        this.state = TaskState.PENDING;
        this.message = message;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.progress = 0;
    }
}
