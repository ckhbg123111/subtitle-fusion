package com.zhongjia.subtitlefusion.service;

import com.zhongjia.subtitlefusion.model.TaskInfo;
import com.zhongjia.subtitlefusion.model.TaskState;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 分布式任务存储服务
 * 基于Redis实现跨节点的任务状态管理
 */
@Service
@ConditionalOnProperty(name = "task.storage.type", havingValue = "redis")
@Slf4j
public class DistributedTaskStorageService {

    private static final String TASK_KEY_PREFIX = "subtitle_task:";
    private static final String TASK_LOCK_PREFIX = "task_lock:";
    private static final String NODE_TASKS_KEY = "node_tasks:";
    private static final long LOCK_TIMEOUT = 300; // 5分钟锁超时

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    private final String nodeId = generateNodeId();

    /**
     * 创建新任务
     */
    public TaskInfo createTask(String taskId) throws Exception {
        String lockKey = TASK_LOCK_PREFIX + taskId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试获取分布式锁
            if (lock.tryLock(10, LOCK_TIMEOUT, TimeUnit.SECONDS)) {
                // 检查任务是否已存在
                if (taskExists(taskId)) {
                    throw new IllegalArgumentException("任务ID已存在: " + taskId);
                }

                TaskInfo taskInfo = new TaskInfo(taskId);
                taskInfo.setProcessingNodeId(nodeId);
                
                // 保存任务信息到Redis
                String taskKey = TASK_KEY_PREFIX + taskId;
                redisTemplate.opsForValue().set(taskKey, taskInfo, 24, TimeUnit.HOURS);
                
                // 记录节点任务映射
                String nodeTasksKey = NODE_TASKS_KEY + nodeId;
                redisTemplate.opsForSet().add(nodeTasksKey, taskId);
                redisTemplate.expire(nodeTasksKey, 25, TimeUnit.HOURS);

                log.info("节点 {} 创建新任务: {}", nodeId, taskId);
                return taskInfo;
            } else {
                throw new RuntimeException("获取任务锁超时: " + taskId);
            }
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 获取任务信息
     */
    public TaskInfo getTask(String taskId) {
        String taskKey = TASK_KEY_PREFIX + taskId;
        Object obj = redisTemplate.opsForValue().get(taskKey);
        return obj != null ? (TaskInfo) obj : null;
    }

    /**
     * 检查任务是否存在
     */
    public boolean taskExists(String taskId) {
        String taskKey = TASK_KEY_PREFIX + taskId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(taskKey));
    }

    /**
     * 更新任务状态
     */
    public void updateTaskState(String taskId, TaskState state) {
        updateTaskWithLock(taskId, taskInfo -> {
            taskInfo.setState(state);
            return taskInfo;
        });
    }

    /**
     * 更新任务进度
     */
    public void updateTaskProgress(String taskId, TaskState state, int progress, String message) {
        updateTaskWithLock(taskId, taskInfo -> {
            taskInfo.updateProgress(state, progress, message);
            return taskInfo;
        });
    }

    /**
     * 标记任务完成
     */
    public void markTaskCompleted(String taskId, String outputUrl) {
        updateTaskWithLock(taskId, taskInfo -> {
            taskInfo.markCompleted(outputUrl);
            return taskInfo;
        });
    }

    /**
     * 标记任务完成（带素材资源压缩包URL）
     */
    public void markTaskCompleted(String taskId, String outputUrl, String resourcePackageZipUrl) {
        updateTaskWithLock(taskId, taskInfo -> {
            taskInfo.markCompleted(outputUrl, resourcePackageZipUrl);
            return taskInfo;
        });
    }

    /**
     * 标记任务失败
     */
    public void markTaskFailed(String taskId, String errorMessage) {
        updateTaskWithLock(taskId, taskInfo -> {
            taskInfo.markFailed(errorMessage);
            return taskInfo;
        });
    }

    /**
     * 删除任务
     */
    public void removeTask(String taskId) {
        String lockKey = TASK_LOCK_PREFIX + taskId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (lock.tryLock(5, 60, TimeUnit.SECONDS)) {
                String taskKey = TASK_KEY_PREFIX + taskId;
                TaskInfo taskInfo = getTask(taskId);
                
                if (taskInfo != null) {
                    // 从节点任务集合中移除
                    String nodeTasksKey = NODE_TASKS_KEY + taskInfo.getProcessingNodeId();
                    redisTemplate.opsForSet().remove(nodeTasksKey, taskId);
                    
                    // 删除任务
                    redisTemplate.delete(taskKey);
                    log.info("删除任务: {}", taskId);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("删除任务被中断: {}", taskId, e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 获取当前节点的任务总数
     */
    public int getNodeTaskCount() {
        String nodeTasksKey = NODE_TASKS_KEY + nodeId;
        Set<Object> taskIds = redisTemplate.opsForSet().members(nodeTasksKey);
        return taskIds != null ? taskIds.size() : 0;
    }

    /**
     * 获取全局任务总数
     */
    public int getGlobalTaskCount() {
        Set<String> taskKeys = redisTemplate.keys(TASK_KEY_PREFIX + "*");
        return taskKeys != null ? taskKeys.size() : 0;
    }

    /**
     * 清理过期任务
     */
    public void cleanupExpiredTasks() {
        Set<String> taskKeys = redisTemplate.keys(TASK_KEY_PREFIX + "*");
        if (taskKeys == null) return;

        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
        int cleanedCount = 0;

        for (String taskKey : taskKeys) {
            TaskInfo taskInfo = (TaskInfo) redisTemplate.opsForValue().get(taskKey);
            if (taskInfo != null && shouldCleanupTask(taskInfo, cutoffTime)) {
                String taskId = taskInfo.getTaskId();
                removeTask(taskId);
                cleanedCount++;
            }
        }

        if (cleanedCount > 0) {
            log.info("清理了 {} 个过期任务", cleanedCount);
        }
    }

    /**
     * 恢复僵尸任务（处理节点已下线但任务还在处理中）
     */
    public void recoverZombieTasks() {
        Set<String> taskKeys = redisTemplate.keys(TASK_KEY_PREFIX + "*");
        if (taskKeys == null) return;

        LocalDateTime zombieThreshold = LocalDateTime.now().minusMinutes(30);
        int recoveredCount = 0;

        for (String taskKey : taskKeys) {
            TaskInfo taskInfo = (TaskInfo) redisTemplate.opsForValue().get(taskKey);
            if (taskInfo != null && isZombieTask(taskInfo, zombieThreshold)) {
                // 重置任务状态为PENDING，允许其他节点处理
                updateTaskWithLock(taskInfo.getTaskId(), task -> {
                    task.setState(TaskState.PENDING);
                    task.setMessage("任务重新排队 - 原处理节点可能已下线");
                    task.setProcessingNodeId(null);
                    return task;
                });
                recoveredCount++;
                log.info("恢复僵尸任务: {}", taskInfo.getTaskId());
            }
        }

        if (recoveredCount > 0) {
            log.info("恢复了 {} 个僵尸任务", recoveredCount);
        }
    }

    /**
     * 更新任务的草稿下载地址
     */
    public void updateTaskDraftUrl(String taskId, String draftUrl) {
        updateTaskWithLock(taskId, taskInfo -> {
            taskInfo.setDraftUrl(draftUrl);
            return taskInfo;
        });
    }

    /**
     * 更新云渲染任务ID（用于对外查询云侧任务进度）
     */
    public void updateTaskCloudTaskId(String taskId, String cloudTaskId) {
        updateTaskWithLock(taskId, taskInfo -> {
            taskInfo.setCloudTaskId(cloudTaskId);
            return taskInfo;
        });
    }

    /**
     * 获取当前节点ID
     */
    public String getNodeId() {
        return nodeId;
    }

    // 私有方法

    /**
     * 带锁的任务更新操作
     */
    private void updateTaskWithLock(String taskId, TaskUpdater updater) {
        String lockKey = TASK_LOCK_PREFIX + taskId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (lock.tryLock(5, 60, TimeUnit.SECONDS)) {
                TaskInfo taskInfo = getTask(taskId);
                if (taskInfo != null) {
                    TaskInfo updatedTask = updater.update(taskInfo);
                    String taskKey = TASK_KEY_PREFIX + taskId;
                    redisTemplate.opsForValue().set(taskKey, updatedTask, 24, TimeUnit.HOURS);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("更新任务被中断: {}", taskId, e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 生成节点ID
     */
    private String generateNodeId() {
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            long timestamp = System.currentTimeMillis() % 100000;
            return hostName + "-" + timestamp;
        } catch (Exception e) {
            return "node-" + System.currentTimeMillis() % 100000;
        }
    }

    /**
     * 判断是否应该清理任务
     */
    private boolean shouldCleanupTask(TaskInfo task, LocalDateTime cutoffTime) {
        return (task.getState() == TaskState.COMPLETED || task.getState() == TaskState.FAILED) 
               && task.getUpdateTime().isBefore(cutoffTime);
    }

    /**
     * 判断是否为僵尸任务
     */
    private boolean isZombieTask(TaskInfo task, LocalDateTime threshold) {
        return (task.getState() == TaskState.DOWNLOADING || 
                task.getState() == TaskState.PROCESSING || 
                task.getState() == TaskState.UPLOADING) 
               && task.getUpdateTime().isBefore(threshold);
    }

    /**
     * 任务更新接口
     */
    @FunctionalInterface
    private interface TaskUpdater {
        TaskInfo update(TaskInfo taskInfo);
    }
}
