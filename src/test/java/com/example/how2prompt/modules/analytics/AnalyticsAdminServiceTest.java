package com.example.how2prompt.modules.analytics;

import com.example.how2prompt.modules.analytics.dto.AnalyticsDashboardResponse;
import com.example.how2prompt.modules.analytics.service.AnalyticsAdminService;
import com.example.how2prompt.modules.identity.repository.UserRepository;
import com.example.how2prompt.modules.prompt.repository.GeneratedPromptRepository;
import com.example.how2prompt.modules.template.entity.Template;
import com.example.how2prompt.modules.template.repository.TemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsAdminServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private GeneratedPromptRepository generatedPromptRepository;
    @Mock private TemplateRepository templateRepository;
    @Mock private StringRedisTemplate redis;
    @Mock private ValueOperations<String, String> valueOperations;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private AnalyticsAdminService analyticsAdminService;

    @BeforeEach
    void setUp() {
        lenient().when(redis.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testGetDashboardMetrics_CacheHit() throws Exception {
        String cachedJson = "{\"dau\":10}";
        when(valueOperations.get("admin:analytics:dashboard")).thenReturn(cachedJson);
        when(objectMapper.readValue(cachedJson, AnalyticsDashboardResponse.class))
                .thenReturn(AnalyticsDashboardResponse.builder().dau(10L).build());

        AnalyticsDashboardResponse res = analyticsAdminService.getDashboardMetrics();
        assertNotNull(res);
        verify(userRepository, never()).countActiveUsersSince(any());
    }

    @Test
    void testGetDashboardMetrics_CacheMiss_ExceptionOnRead() throws Exception {
        when(valueOperations.get("admin:analytics:dashboard")).thenThrow(new RuntimeException("Redis error"));
        
        setupRepositoryMocks();
        
        AnalyticsDashboardResponse res = analyticsAdminService.getDashboardMetrics();
        assertNotNull(res);
        verify(userRepository, times(3)).countActiveUsersSince(any());
    }
    
    @Test
    void testGetDashboardMetrics_CacheMiss_ExceptionOnWrite() throws Exception {
        when(valueOperations.get("admin:analytics:dashboard")).thenReturn(null);
        setupRepositoryMocks();
        when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException("JSON error"));
        
        AnalyticsDashboardResponse res = analyticsAdminService.getDashboardMetrics();
        assertNotNull(res);
        verify(valueOperations, never()).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void testGetDashboardMetrics_WithBadRows() throws Exception {
        when(valueOperations.get("admin:analytics:dashboard")).thenReturn(null);

        when(userRepository.countActiveUsersSince(any(Instant.class))).thenReturn(10L);
        when(userRepository.count()).thenReturn(100L);
        when(userRepository.countVerifiedUsers()).thenReturn(80L);
        when(generatedPromptRepository.countDistinctUsersWithGenerations()).thenReturn(50L);

        Template t = new Template();
        t.setId(UUID.randomUUID());
        t.setUsageCount(5);
        when(templateRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(t)));

        List<Object[]> badPrompts = new ArrayList<>();
        badPrompts.add(null);
        badPrompts.add(new Object[]{"2023-01-01"}); // length 1
        badPrompts.add(new Object[]{null, 5L}); // row[0] is null
        badPrompts.add(new Object[]{"2023-01-01", 10L}); // valid
        when(generatedPromptRepository.countPromptsPerDaySince(any(Instant.class))).thenReturn(badPrompts);

        List<Object[]> badModels = new ArrayList<>();
        badModels.add(null);
        badModels.add(new Object[]{UUID.randomUUID(), "Model"}); // length 2
        badModels.add(new Object[]{null, "Model", 10L}); // row[0] is null
        badModels.add(new Object[]{UUID.randomUUID(), "Valid Model", 20L}); // valid
        when(generatedPromptRepository.countMostUsedModels(5)).thenReturn(badModels);

        AnalyticsDashboardResponse res = analyticsAdminService.getDashboardMetrics();
        assertNotNull(res);
    }

    private void setupRepositoryMocks() {
        when(userRepository.countActiveUsersSince(any(Instant.class))).thenReturn(10L);
        when(userRepository.count()).thenReturn(100L);
        when(userRepository.countVerifiedUsers()).thenReturn(80L);
        when(generatedPromptRepository.countDistinctUsersWithGenerations()).thenReturn(50L);
        when(templateRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
        when(generatedPromptRepository.countPromptsPerDaySince(any(Instant.class))).thenReturn(List.of());
        when(generatedPromptRepository.countMostUsedModels(5)).thenReturn(List.of());
    }
}
