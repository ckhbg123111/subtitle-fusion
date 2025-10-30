package com.zhongjia.subtitlefusion.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * 异步处理配置
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

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

        // 透传MDC（包含traceId）到子线程
        executor.setTaskDecorator(mdcTaskDecorator());
        
        executor.initialize();
        return executor;
    }

    /**
     * 默认 @Async 使用的线程池，统一走带有MDC透传的线程池
     */
    @Override
    public Executor getAsyncExecutor() {
        return subtitleTaskExecutor();
    }

    private TaskDecorator mdcTaskDecorator() {
        return runnable -> {
            Map<String, String> contextMap = MDC.getCopyOfContextMap();
            return () -> {
                Map<String, String> previous = MDC.getCopyOfContextMap();
                try {
                    if (contextMap != null) {
                        MDC.setContextMap(contextMap);
                    } else {
                        MDC.clear();
                    }
                    runnable.run();
                } finally {
                    if (previous != null) {
                        MDC.setContextMap(previous);
                    } else {
                        MDC.clear();
                    }
                }
            };
        };
    }
}
