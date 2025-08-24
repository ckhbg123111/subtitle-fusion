package com.zhongjia.subtitlefusion.model;

public class SubtitleFusionResponse {
    private String outputPath;
    private String message;

    public SubtitleFusionResponse() {}

    public SubtitleFusionResponse(String outputPath, String message) {
        this.outputPath = outputPath;
        this.message = message;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}


