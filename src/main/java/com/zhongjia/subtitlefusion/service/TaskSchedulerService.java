package com.zhongjia.subtitlefusion.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 任务调度服务
 * 负责定时清理过期任务和恢复僵尸任务
 */
@Service
@EnableScheduling
@ConditionalOnProperty(name = "task.storage.type", havingValue = "redis")
public class TaskSchedulerService {

    @Autowired
    private DistributedTaskStorageService distributedTaskStorage;

    /**
     * 每30分钟执行一次僵尸任务恢复
     */
    @Scheduled(fixedDelay = 30 * 60 * 1000, initialDelay = 60 * 1000)
    public void recoverZombieTasks() {
        try {
            System.out.println("开始僵尸任务恢复检查...");
            distributedTaskStorage.recoverZombieTasks();
        } catch (Exception e) {
            System.err.println("僵尸任务恢复失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 每1小时执行一次过期任务清理
     */
    @Scheduled(fixedDelay = 60 * 60 * 1000, initialDelay = 5 * 60 * 1000)
    public void cleanupExpiredTasks() {
        try {
            System.out.println("开始过期任务清理...");
            distributedTaskStorage.cleanupExpiredTasks();
        } catch (Exception e) {
            System.err.println("过期任务清理失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 每5分钟输出一次节点状态
     */
    @Scheduled(fixedDelay = 5 * 60 * 1000, initialDelay = 30 * 1000)
    public void reportNodeStatus() {
        try {
            String nodeId = distributedTaskStorage.getNodeId();
            int nodeTaskCount = distributedTaskStorage.getNodeTaskCount();
            int globalTaskCount = distributedTaskStorage.getGlobalTaskCount();
            
            System.out.println(String.format(
                "节点状态报告 - 节点ID: %s, 本节点任务数: %d, 全局任务数: %d", 
                nodeId, nodeTaskCount, globalTaskCount
            ));
        } catch (Exception e) {
            System.err.println("节点状态报告失败: " + e.getMessage());
        }
    }
}
