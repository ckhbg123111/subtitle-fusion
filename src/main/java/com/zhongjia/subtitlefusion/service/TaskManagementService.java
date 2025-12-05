package com.zhongjia.subtitlefusion.service;

import com.zhongjia.subtitlefusion.model.TaskInfo;
import com.zhongjia.subtitlefusion.model.TaskState;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 任务管理服务
 * 负责管理异步任务的状态和进度
 */
@Service
public class TaskManagementService {

    // 使用内存存储任务信息，生产环境建议使用Redis
    private final ConcurrentMap<String, TaskInfo> taskStorage = new ConcurrentHashMap<>();

    /**
     * 创建新任务
     */
    public TaskInfo createTask(String taskId) {
        if (taskStorage.containsKey(taskId)) {
            throw new IllegalArgumentException("任务ID已存在: " + taskId);
        }

        TaskInfo taskInfo = new TaskInfo(taskId);
        taskStorage.put(taskId, taskInfo);
        // 统一由上层服务输出日志，避免重复
        return taskInfo;
    }

    /**
     * 获取任务信息
     */
    public TaskInfo getTask(String taskId) {
        return taskStorage.get(taskId);
    }

    /**
     * 检查任务是否存在
     */
    public boolean taskExists(String taskId) {
        return taskStorage.containsKey(taskId);
    }

    /**
     * 更新任务状态
     */
    public void updateTaskState(String taskId, TaskState state) {
        TaskInfo taskInfo = taskStorage.get(taskId);
        if (taskInfo != null) {
            taskInfo.setState(state);
            // 日志在上层服务统一输出
        }
    }

    /**
     * 更新任务进度
     */
    public void updateTaskProgress(String taskId, TaskState state, int progress, String message) {
        TaskInfo taskInfo = taskStorage.get(taskId);
        if (taskInfo != null) {
            taskInfo.updateProgress(state, progress, message);
            // 日志在上层服务统一输出
        }
    }

    /**
     * 标记任务完成
     */
    public void markTaskCompleted(String taskId, String outputUrl) {
        TaskInfo taskInfo = taskStorage.get(taskId);
        if (taskInfo != null) {
            taskInfo.markCompleted(outputUrl);
            // 日志在上层服务统一输出
        }
    }

    /**
     * 标记任务完成（带素材资源压缩包URL）
     */
    public void markTaskCompleted(String taskId, String outputUrl, String resourcePackageZipUrl) {
        TaskInfo taskInfo = taskStorage.get(taskId);
        if (taskInfo != null) {
            taskInfo.markCompleted(outputUrl, resourcePackageZipUrl);
        }
    }

    /**
     * 标记任务失败
     */
    public void markTaskFailed(String taskId, String errorMessage) {
        TaskInfo taskInfo = taskStorage.get(taskId);
        if (taskInfo != null) {
            taskInfo.markFailed(errorMessage);
            // 日志在上层服务统一输出
        }
    }

    /**
     * 删除任务（可选，用于清理过期任务）
     */
    public void removeTask(String taskId) {
        TaskInfo removed = taskStorage.remove(taskId);
        if (removed != null) {
            // 日志在上层服务统一输出
        }
    }

    /**
     * 更新任务的草稿下载地址
     */
    public void updateTaskDraftUrl(String taskId, String draftUrl) {
        TaskInfo taskInfo = taskStorage.get(taskId);
        if (taskInfo != null) {
            taskInfo.setDraftUrl(draftUrl);
        }
    }

    /**
     * 获取任务总数
     */
    public int getTaskCount() {
        return taskStorage.size();
    }

    /**
     * 清理过期任务（超过24小时的已完成或失败任务）
     */
    public void cleanupExpiredTasks() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
        taskStorage.entrySet().removeIf(entry -> {
            TaskInfo task = entry.getValue();
            return (task.getState() == TaskState.COMPLETED || task.getState() == TaskState.FAILED) 
                   && task.getUpdateTime().isBefore(cutoffTime);
        });
    }
}
