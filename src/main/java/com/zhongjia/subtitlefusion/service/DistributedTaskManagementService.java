package com.zhongjia.subtitlefusion.service;

import com.zhongjia.subtitlefusion.model.TaskInfo;
import com.zhongjia.subtitlefusion.model.TaskState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 分布式任务管理服务
 * 统一调用TaskStorageInterface，支持内存和Redis两种存储方式
 */
@Service
public class DistributedTaskManagementService {

    @Autowired
    private TaskStorageInterface taskStorage;

    /**
     * 创建新任务
     */
    public TaskInfo createTask(String taskId) throws Exception {
        TaskInfo task = taskStorage.createTask(taskId);
        System.out.println("创建新任务: " + taskId);
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
        System.out.println("任务 " + taskId + " 状态更新为: " + state.getDescription());
    }

    /**
     * 更新任务进度
     */
    public void updateTaskProgress(String taskId, TaskState state, int progress, String message) {
        taskStorage.updateTaskProgress(taskId, state, progress, message);
        System.out.println("任务 " + taskId + " 进度: " + progress + "% - " + message);
    }

    /**
     * 标记任务完成
     */
    public void markTaskCompleted(String taskId, String outputUrl) {
        taskStorage.markTaskCompleted(taskId, outputUrl);
        System.out.println("任务 " + taskId + " 完成，输出URL: " + outputUrl);
    }

    /**
     * 标记任务完成（带素材资源压缩包URL）
     */
    public void markTaskCompleted(String taskId, String outputUrl, String resourcePackageZipUrl) {
        taskStorage.markTaskCompleted(taskId, outputUrl, resourcePackageZipUrl);
        System.out.println("任务 " + taskId + " 完成，输出URL: " + outputUrl + ", 资源包: " + resourcePackageZipUrl);
    }

    /**
     * 标记任务失败
     */
    public void markTaskFailed(String taskId, String errorMessage) {
        taskStorage.markTaskFailed(taskId, errorMessage);
        System.err.println("任务 " + taskId + " 失败: " + errorMessage);
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
