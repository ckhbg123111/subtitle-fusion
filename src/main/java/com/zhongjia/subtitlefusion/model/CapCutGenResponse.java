package com.zhongjia.subtitlefusion.model;

import lombok.Data;

@Data
public class CapCutGenResponse {
    private boolean success;
    private String draftId;
    private String draftUrl;
    private String message;
}


