package com.zhongjia.subtitlefusion.model.capcut;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CapCutResponse<T> {
    private boolean success;
    private String error;
    @JsonProperty("purchase_link")
    private String purchaseLink;
    private T output;
}


