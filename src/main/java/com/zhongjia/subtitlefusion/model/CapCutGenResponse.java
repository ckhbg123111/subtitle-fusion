package com.zhongjia.subtitlefusion.model;

import lombok.Data;

@Data
public class CapCutGenResponse {
    private boolean success;
    private String draftId;
    private String draftUrl;
    private String message;
    // 是否走云渲染
    private Boolean cloudRendering;
    // 云渲染任务ID（当 cloudRendering=true 时返回）
    private String taskId;
}


