package com.zhongjia.subtitlefusion.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务信息 - 支持Redis序列化
 */
public class TaskInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private String taskId;
    private TaskState state;
    private String message;
    private String outputUrl;
    private String errorMessage;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime updateTime;
    
    private int progress; // 进度百分比 0-100
    private String processingNodeId; // 处理节点ID

    public TaskInfo() {}

    public TaskInfo(String taskId) {
        this.taskId = taskId;
        this.state = TaskState.PENDING;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.progress = 0;
        this.message = "任务已创建，等待处理";
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
        this.updateTime = LocalDateTime.now();
        this.message = state.getDescription();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
        this.updateTime = LocalDateTime.now();
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
        this.progress = Math.max(0, Math.min(100, progress));
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 更新状态和进度
     */
    public void updateProgress(TaskState state, int progress, String message) {
        this.state = state;
        this.progress = progress;
        this.message = message;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 标记任务完成
     */
    public void markCompleted(String outputUrl) {
        this.state = TaskState.COMPLETED;
        this.outputUrl = outputUrl;
        this.progress = 100;
        this.message = "处理完成";
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 标记任务失败
     */
    public void markFailed(String errorMessage) {
        this.state = TaskState.FAILED;
        this.errorMessage = errorMessage;
        this.message = "处理失败: " + errorMessage;
        this.updateTime = LocalDateTime.now();
    }

    public String getProcessingNodeId() {
        return processingNodeId;
    }

    public void setProcessingNodeId(String processingNodeId) {
        this.processingNodeId = processingNodeId;
    }
}
