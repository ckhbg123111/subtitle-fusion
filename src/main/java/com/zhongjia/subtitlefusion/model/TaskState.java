package com.zhongjia.subtitlefusion.model;

/**
 * 任务状态枚举
 */
public enum TaskState {
    PENDING("等待处理"),
    DOWNLOADING("下载视频中"),
    PROCESSING("字幕渲染中"), 
    UPLOADING("上传到MinIO中"),
    COMPLETED("处理完成"),
    FAILED("处理失败");

    private final String description;

    TaskState(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
