package com.zhongjia.subtitlefusion.model;

import lombok.Data;

/**
 * 云渲染结果查询返回
 */
@Data
public class CapCutCloudResponse<T> {

    private Boolean success;
    /**
     * 网络错误信息
     */
    private String error;
    private T output;
}
