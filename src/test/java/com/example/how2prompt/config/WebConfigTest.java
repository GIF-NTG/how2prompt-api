package com.example.how2prompt.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WebConfigTest {

    @Test
    void configurePathMatch() {
        AuthProperties props = new AuthProperties();
        WebConfig config = new WebConfig(props);
        
        PathMatchConfigurer configurer = mock(PathMatchConfigurer.class);
        config.configurePathMatch(configurer);
        
        verify(configurer).addPathPrefix(anyString(), any());
    }

    @Test
    void addCorsMappings_NoOrigins() {
        AuthProperties props = new AuthProperties();
        props.setCorsAllowedOrigins(Collections.emptyList());
        WebConfig config = new WebConfig(props);
        
        CorsRegistry registry = mock(CorsRegistry.class);
        config.addCorsMappings(registry);
        
        // Should not interact with registry
        verify(registry, org.mockito.Mockito.never()).addMapping(anyString());
    }
    
    @Test
    void addCorsMappings_WithOrigins() {
        AuthProperties props = new AuthProperties();
        props.setCorsAllowedOrigins(List.of("http://localhost:3000"));
        WebConfig config = new WebConfig(props);
        
        CorsRegistry registry = mock(CorsRegistry.class);
        CorsRegistration registration = mock(CorsRegistration.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        
        when(registry.addMapping(anyString())).thenReturn(registration);

        
        config.addCorsMappings(registry);
        
        verify(registry).addMapping("/api/v1/**");
        verify(registration).allowedOrigins("http://localhost:3000");
    }
}
