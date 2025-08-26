package com.zhongjia.subtitlefusion.model;

import java.time.LocalDateTime;

/**
 * 任务响应
 */
public class TaskResponse {
    private String taskId;
    private TaskState state;
    private String message;
    private String outputUrl;
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

    // Getters and Setters
    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public TaskState getState() {
        return state;
    }

    public void setState(TaskState state) {
        this.state = state;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getOutputUrl() {
        return outputUrl;
    }

    public void setOutputUrl(String outputUrl) {
        this.outputUrl = outputUrl;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
