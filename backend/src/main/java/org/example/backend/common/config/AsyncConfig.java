package org.example.backend.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * AI 索引: 后台异步任务线程池配置。
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "kyzzExamGradingTaskExecutor")
    public Executor kyzzExamGradingTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("kyzz-exam-grading-");
        executor.initialize();
        return executor;
    }
}
