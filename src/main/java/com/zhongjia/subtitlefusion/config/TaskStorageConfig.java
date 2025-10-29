package com.zhongjia.subtitlefusion.config;

import com.zhongjia.subtitlefusion.service.DistributedTaskStorageService;
import com.zhongjia.subtitlefusion.service.TaskManagementService;
import com.zhongjia.subtitlefusion.service.TaskStorageInterface;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 任务存储配置
 * 根据配置选择使用内存存储还是Redis分布式存储
 */
@Configuration
public class TaskStorageConfig {

    @Bean
    @Primary
    @ConditionalOnProperty(name = "task.storage.type", havingValue = "redis", matchIfMissing = false)
    public TaskStorageInterface distributedTaskStorage(DistributedTaskStorageService distributedService) {
        return new TaskStorageInterface() {
            @Override
            public com.zhongjia.subtitlefusion.model.TaskInfo createTask(String taskId) throws Exception {
                return distributedService.createTask(taskId);
            }

            @Override
            public com.zhongjia.subtitlefusion.model.TaskInfo getTask(String taskId) {
                return distributedService.getTask(taskId);
            }

            @Override
            public boolean taskExists(String taskId) {
                return distributedService.taskExists(taskId);
            }

            @Override
            public void updateTaskState(String taskId, com.zhongjia.subtitlefusion.model.TaskState state) {
                distributedService.updateTaskState(taskId, state);
            }

            @Override
            public void updateTaskProgress(String taskId, com.zhongjia.subtitlefusion.model.TaskState state, int progress, String message) {
                distributedService.updateTaskProgress(taskId, state, progress, message);
            }

            @Override
            public void markTaskCompleted(String taskId, String outputUrl) {
                distributedService.markTaskCompleted(taskId, outputUrl);
            }

            @Override
            public void markTaskCompleted(String taskId, String outputUrl, String resourcePackageZipUrl) {
                distributedService.markTaskCompleted(taskId, outputUrl, resourcePackageZipUrl);
            }

            @Override
            public void markTaskFailed(String taskId, String errorMessage) {
                distributedService.markTaskFailed(taskId, errorMessage);
            }

            @Override
            public void removeTask(String taskId) {
                distributedService.removeTask(taskId);
            }

            @Override
            public int getTaskCount() {
                return distributedService.getGlobalTaskCount();
            }

            @Override
            public void cleanupExpiredTasks() {
                distributedService.cleanupExpiredTasks();
            }
        };
    }

    @Bean
    @ConditionalOnProperty(name = "task.storage.type", havingValue = "memory", matchIfMissing = true)
    public TaskStorageInterface memoryTaskStorage(TaskManagementService memoryService) {
        return new TaskStorageInterface() {
            @Override
            public com.zhongjia.subtitlefusion.model.TaskInfo createTask(String taskId) throws Exception {
                return memoryService.createTask(taskId);
            }

            @Override
            public com.zhongjia.subtitlefusion.model.TaskInfo getTask(String taskId) {
                return memoryService.getTask(taskId);
            }

            @Override
            public boolean taskExists(String taskId) {
                return memoryService.taskExists(taskId);
            }

            @Override
            public void updateTaskState(String taskId, com.zhongjia.subtitlefusion.model.TaskState state) {
                memoryService.updateTaskState(taskId, state);
            }

            @Override
            public void updateTaskProgress(String taskId, com.zhongjia.subtitlefusion.model.TaskState state, int progress, String message) {
                memoryService.updateTaskProgress(taskId, state, progress, message);
            }

            @Override
            public void markTaskCompleted(String taskId, String outputUrl) {
                memoryService.markTaskCompleted(taskId, outputUrl);
            }

            @Override
            public void markTaskCompleted(String taskId, String outputUrl, String resourcePackageZipUrl) {
                memoryService.markTaskCompleted(taskId, outputUrl, resourcePackageZipUrl);
            }

            @Override
            public void markTaskFailed(String taskId, String errorMessage) {
                memoryService.markTaskFailed(taskId, errorMessage);
            }

            @Override
            public void removeTask(String taskId) {
                memoryService.removeTask(taskId);
            }

            @Override
            public int getTaskCount() {
                return memoryService.getTaskCount();
            }

            @Override
            public void cleanupExpiredTasks() {
                memoryService.cleanupExpiredTasks();
            }
        };
    }
}
