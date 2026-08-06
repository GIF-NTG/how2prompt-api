package com.example.how2prompt.config;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AsyncConfigTest {

    @Test
    void taskExecutor() {
        AsyncConfig config = new AsyncConfig();
        Executor executor = config.taskExecutor();

        assertNotNull(executor);
        assertInstanceOf(ThreadPoolTaskExecutor.class, executor);

        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) executor;
        assertEquals(10, taskExecutor.getCorePoolSize());
        assertEquals(50, taskExecutor.getMaxPoolSize());
        assertEquals("AsyncExec-", taskExecutor.getThreadNamePrefix());
    }
}
