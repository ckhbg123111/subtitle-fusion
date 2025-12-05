package com.zhongjia.subtitlefusion.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
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
     * 兼容第三方返回 result 可能为 String 或 Object 的情况：
     * 内部保存原始对象，对外通过 getResult() 提供字符串 URL。
     */
    @JsonIgnore
    private Object rawResult;

    @JsonProperty("result")
    public void setResult(Object result) {
        this.rawResult = result;
    }

    @JsonProperty("result")
    public String getResult() {
        if (rawResult == null) return null;
        if (rawResult instanceof String) return (String) rawResult;
        if (rawResult instanceof Map) {
            Map<?, ?> m = (Map<?, ?>) rawResult;
            Object url = m.get("url");
            if (url == null) url = m.get("download_url");
            if (url == null) url = m.get("play_url");
            if (url == null) url = m.get("videoUrl");
            if (url == null) url = m.get("video_url");
            return url != null ? String.valueOf(url) : null;
        }
        return String.valueOf(rawResult);
    }

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


