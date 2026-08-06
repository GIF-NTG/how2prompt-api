package com.example.how2prompt.config;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class RedisConfigTest {

    @Test
    void stringRedisTemplate() {
        RedisConfig config = new RedisConfig();
        RedisConnectionFactory factory = mock(RedisConnectionFactory.class);
        
        StringRedisTemplate template = config.stringRedisTemplate(factory);
        assertNotNull(template);
    }
}
