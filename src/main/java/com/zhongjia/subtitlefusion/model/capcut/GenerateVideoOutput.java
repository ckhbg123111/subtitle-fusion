package com.zhongjia.subtitlefusion.model.capcut;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GenerateVideoOutput {
    private String error;
    private boolean success;
    @JsonProperty("task_id")
    private String taskId;
}


