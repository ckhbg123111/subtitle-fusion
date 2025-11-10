package com.zhongjia.subtitlefusion.model.capcut;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DraftRefOutput {
    @JsonProperty("draft_id")
    private String draftId;
    @JsonProperty("draft_url")
    private String draftUrl;
}


