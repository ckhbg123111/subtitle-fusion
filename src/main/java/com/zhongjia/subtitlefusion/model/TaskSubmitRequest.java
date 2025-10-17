package com.zhongjia.subtitlefusion.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 异步任务提交请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskSubmitRequest {
    private String taskId;
    private String videoUrl;
}
