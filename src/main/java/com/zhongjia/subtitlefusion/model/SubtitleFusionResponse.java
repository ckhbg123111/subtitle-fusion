package com.zhongjia.subtitlefusion.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubtitleFusionResponse {
    private String outputPath;
    private String message;
}
