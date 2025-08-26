package com.zhongjia.subtitlefusion.model;

/**
 * 异步任务提交请求
 */
public class TaskSubmitRequest {
    private String taskId;
    private String videoUrl;

    public TaskSubmitRequest() {}

    public TaskSubmitRequest(String taskId, String videoUrl) {
        this.taskId = taskId;
        this.videoUrl = videoUrl;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}
