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
    // 新增素材资源压缩包
    private String resourcePackageZipUrl;
    private String errorMessage;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private int progress;

    public TaskResponse() {}

    public TaskResponse(TaskInfo taskInfo) {
        this.taskId = taskInfo.getTaskId();
        this.state = taskInfo.getState();
        this.message = taskInfo.getMessage();
        this.outputUrl = taskInfo.getOutputUrl();
        this.resourcePackageZipUrl = taskInfo.getResourcePackageZipUrl();
        this.errorMessage = taskInfo.getErrorMessage();
        this.createTime = taskInfo.getCreateTime();
        this.updateTime = taskInfo.getUpdateTime();
        this.progress = taskInfo.getProgress();
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
