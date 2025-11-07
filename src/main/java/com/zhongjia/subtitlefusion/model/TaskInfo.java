package com.zhongjia.subtitlefusion.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务信息 - 支持Redis序列化
 */
@Data
public class TaskInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private String taskId;
    private TaskState state;
    private String message;
    private String outputUrl;
    // 新增：素材资源压缩包下载地址
    private String resourcePackageZipUrl;
    private String errorMessage;
    // 新增：云渲染任务ID（用于对外查询云任务进度）
    private String cloudTaskId;
    
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

    // 定制 setter：保留原有副作用逻辑
    public void setState(TaskState state) {
        this.state = state;
        this.updateTime = LocalDateTime.now();
        this.message = state.getDescription();
    }

    public void setMessage(String message) {
        this.message = message;
        this.updateTime = LocalDateTime.now();
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
     * 标记任务完成（带素材资源压缩包）
     */
    public void markCompleted(String outputUrl, String resourcePackageZipUrl) {
        this.state = TaskState.COMPLETED;
        this.outputUrl = outputUrl;
        this.resourcePackageZipUrl = resourcePackageZipUrl;
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
}
