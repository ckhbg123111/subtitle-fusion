package com.zhongjia.subtitlefusion.model;

import lombok.Data;

@Data
public class CapCutCloudTaskStatus {
    private String taskId;
    private boolean success;
    private Integer progress; // 0-100
    private String message;
    private String error;
    private String resultUrl; // 渲染完成后的下载/播放地址
    private String status;    // e.g. PENDING/RUNNING/SUCCESS/FAILED
}


