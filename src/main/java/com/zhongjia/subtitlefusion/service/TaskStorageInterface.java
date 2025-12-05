package com.zhongjia.subtitlefusion.service;

import com.zhongjia.subtitlefusion.model.TaskInfo;
import com.zhongjia.subtitlefusion.model.TaskState;

/**
 * 任务存储接口
 * 统一内存存储和Redis分布式存储的接口
 */
public interface TaskStorageInterface {

    /**
     * 创建新任务
     */
    TaskInfo createTask(String taskId) throws Exception;

    /**
     * 获取任务信息
     */
    TaskInfo getTask(String taskId);

    /**
     * 检查任务是否存在
     */
    boolean taskExists(String taskId);

    /**
     * 更新任务状态
     */
    void updateTaskState(String taskId, TaskState state);

    /**
     * 更新任务进度
     */
    void updateTaskProgress(String taskId, TaskState state, int progress, String message);

    /**
     * 标记任务完成
     */
    void markTaskCompleted(String taskId, String outputUrl);

    /**
     * 标记任务完成（带素材资源压缩包URL）
     */
    default void markTaskCompleted(String taskId, String outputUrl, String resourcePackageZipUrl) {
        // 默认实现：回退为仅写主输出；具体实现可覆盖支持资源包URL
        markTaskCompleted(taskId, outputUrl);
    }

    /**
     * 标记任务失败
     */
    void markTaskFailed(String taskId, String errorMessage);

    /**
     * 删除任务
     */
    void removeTask(String taskId);

    /**
     * 更新任务的草稿下载地址
     */
    void updateTaskDraftUrl(String taskId, String draftUrl);

    /**
     * 获取任务总数
     */
    int getTaskCount();

    /**
     * 清理过期任务
     */
    void cleanupExpiredTasks();
}
