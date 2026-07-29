package com.example.how2prompt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Minimum number of threads kept alive
        executor.setCorePoolSize(10);
        
        // Maximum number of threads before queuing
        executor.setMaxPoolSize(50);
        
        // Size of the queue before rejecting tasks
        // Bounded queue is CRITICAL to prevent OOM
        executor.setQueueCapacity(1000);
        
        executor.setThreadNamePrefix("AsyncExec-");
        
        // Policy when queue is full: 
        // CallerRunsPolicy forces the web thread to execute the save synchronously,
        // providing natural backpressure instead of dropping the data or crashing.
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.initialize();
        return executor;
    }
}
