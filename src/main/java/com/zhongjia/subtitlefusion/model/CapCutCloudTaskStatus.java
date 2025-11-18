package com.zhongjia.subtitlefusion.model;

import lombok.Data;

@Data
public class CapCutCloudTaskStatus {
    /**
     * 当前任务的唯一ID
     */
    private String taskId;
    /**
     * 是否成功，只有当任务成功才会置为true。任务失败，正在处理中都会置为false
     */
    private boolean success;
    /**
     * 导出进度，例如10，20，80等等
     * 0-100
     */
    private Integer progress;
    /**
     * 消息，例如“排队，导出，上传，成功，错误“等等
     */
    private String message;
    /**
     * 业务错误信息
     */
    private String error;
    /**
     * 正确结果，只有当导出成功，这里才会展示导出的视频链接 - 渲染完成后的下载/播放地址
     */
    private String result;
    /**
     * PENDING 排队中
     * PROCESSING 处理素材
     * UPLOADING 上传中
     * SUCCESS 成功
     * DOWNLOADING 下载素材
     * EXPORTING 导出中
     * FAILURE 失败
     */
    private String status;
}


