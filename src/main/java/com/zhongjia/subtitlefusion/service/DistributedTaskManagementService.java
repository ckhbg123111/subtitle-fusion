package com.zhongjia.subtitlefusion.service;

import com.zhongjia.subtitlefusion.model.TaskInfo;
import com.zhongjia.subtitlefusion.model.TaskState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

/**
 * 分布式任务管理服务
 * 统一调用TaskStorageInterface，支持内存和Redis两种存储方式
 */
@Service
@Slf4j
public class DistributedTaskManagementService {

    @Autowired
    private TaskStorageInterface taskStorage;

    /**
     * 创建新任务
     */
    public TaskInfo createTask(String taskId) throws Exception {
        TaskInfo task = taskStorage.createTask(taskId);
        log.info("创建新任务: {}", taskId);
        return task;
    }

    /**
     * 获取任务信息
     */
    public TaskInfo getTask(String taskId) {
        return taskStorage.getTask(taskId);
    }

    /**
     * 检查任务是否存在
     */
    public boolean taskExists(String taskId) {
        return taskStorage.taskExists(taskId);
    }

    /**
     * 更新任务状态
     */
    public void updateTaskState(String taskId, TaskState state) {
        taskStorage.updateTaskState(taskId, state);
        log.info("任务 {} 状态更新为: {}", taskId, state.getDescription());
    }

    /**
     * 更新任务进度
     */
    public void updateTaskProgress(String taskId, TaskState state, int progress, String message) {
        taskStorage.updateTaskProgress(taskId, state, progress, message);
        log.info("任务 {} 进度: {}% - {}", taskId, progress, message);
    }

    /**
     * 标记任务完成
     */
    public void markTaskCompleted(String taskId, String outputUrl) {
        taskStorage.markTaskCompleted(taskId, outputUrl);
        log.info("任务 {} 完成，输出URL: {}", taskId, outputUrl);
    }

    /**
     * 标记任务完成（带素材资源压缩包URL）
     */
    public void markTaskCompleted(String taskId, String outputUrl, String resourcePackageZipUrl) {
        taskStorage.markTaskCompleted(taskId, outputUrl, resourcePackageZipUrl);
        log.info("任务 {} 完成，输出URL: {}, 资源包: {}", taskId, outputUrl, resourcePackageZipUrl);
    }

    /**
     * 标记任务失败
     */
    public void markTaskFailed(String taskId, String errorMessage) {
        taskStorage.markTaskFailed(taskId, errorMessage);
        log.error("任务 {} 失败: {}", taskId, errorMessage);
    }

    /**
     * 删除任务
     */
    public void removeTask(String taskId) {
        taskStorage.removeTask(taskId);
    }

    /**
     * 获取任务总数
     */
    public int getTaskCount() {
        return taskStorage.getTaskCount();
    }

    /**
     * 清理过期任务
     */
    public void cleanupExpiredTasks() {
        taskStorage.cleanupExpiredTasks();
    }
}
