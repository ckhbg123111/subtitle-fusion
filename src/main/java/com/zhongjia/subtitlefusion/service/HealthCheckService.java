package com.zhongjia.subtitlefusion.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查服务
 * 监控Redis连接和任务处理状态
 */
@Component
@ConditionalOnProperty(name = "task.storage.type", havingValue = "redis")
public class HealthCheckService implements HealthIndicator {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private DistributedTaskStorageService distributedTaskStorage;

    @Override
    public Health health() {
        try {
            // 检查Redis连接
            String pingResult = redisTemplate.getConnectionFactory().getConnection().ping();
            
            // 获取任务统计信息
            int globalTaskCount = distributedTaskStorage.getGlobalTaskCount();
            int nodeTaskCount = distributedTaskStorage.getNodeTaskCount();
            String nodeId = distributedTaskStorage.getNodeId();

            Map<String, Object> details = new HashMap<>();
            details.put("redis.ping", pingResult);
            details.put("node.id", nodeId);
            details.put("tasks.global", globalTaskCount);
            details.put("tasks.node", nodeTaskCount);

            return Health.up()
                    .withDetails(details)
                    .build();

        } catch (Exception e) {
            return Health.down()
                    .withException(e)
                    .build();
        }
    }

    /**
     * 获取详细的节点状态信息
     */
    public Map<String, Object> getNodeStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            status.put("nodeId", distributedTaskStorage.getNodeId());
            status.put("globalTaskCount", distributedTaskStorage.getGlobalTaskCount());
            status.put("nodeTaskCount", distributedTaskStorage.getNodeTaskCount());
            status.put("status", "healthy");
            status.put("timestamp", System.currentTimeMillis());
            
            // 检查Redis连接
            String pingResult = redisTemplate.getConnectionFactory().getConnection().ping();
            status.put("redis.status", "PONG".equals(pingResult) ? "connected" : "error");
            
        } catch (Exception e) {
            status.put("status", "error");
            status.put("error", e.getMessage());
        }
        
        return status;
    }
}
