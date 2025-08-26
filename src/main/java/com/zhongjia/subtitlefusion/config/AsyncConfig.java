package com.zhongjia.subtitlefusion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步处理配置
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 配置字幕处理异步线程池
     */
    @Bean(name = "subtitleTaskExecutor")
    public Executor subtitleTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数：同时处理的基础任务数
        executor.setCorePoolSize(2);
        
        // 最大线程数：峰值时的最大处理能力
        executor.setMaxPoolSize(4);
        
        // 队列容量：等待处理的任务队列大小
        executor.setQueueCapacity(100);
        
        // 线程名前缀
        executor.setThreadNamePrefix("SubtitleAsync-");
        
        // 线程空闲时间
        executor.setKeepAliveSeconds(60);
        
        // 拒绝策略：队列满时由调用者执行
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        
        // 等待任务完成后关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(300); // 等待5分钟
        
        executor.initialize();
        return executor;
    }
}
