package com.example.how2prompt.modules.analytics.service;

import com.example.how2prompt.modules.analytics.dto.AnalyticsDashboardResponse;
import com.example.how2prompt.modules.identity.repository.UserRepository;
import com.example.how2prompt.modules.prompt.repository.GeneratedPromptRepository;
import com.example.how2prompt.modules.template.entity.Template;
import com.example.how2prompt.modules.template.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsAdminService {

    private static final String CACHE_KEY = "admin:analytics:dashboard";
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    private final UserRepository userRepository;
    private final GeneratedPromptRepository generatedPromptRepository;
    private final TemplateRepository templateRepository;
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public AnalyticsDashboardResponse getDashboardMetrics() {
        // 1. Kiểm tra cache
        try {
            String cachedJson = redis.opsForValue().get(CACHE_KEY);
            if (cachedJson != null && !cachedJson.isBlank()) {
                return objectMapper.readValue(cachedJson, AnalyticsDashboardResponse.class);
            }
        } catch (Exception e) {
            log.error("Failed to read analytics dashboard from Redis cache: {}", e.getMessage(), e);
        }

        // 2. Tính toán các chỉ số
        Instant now = Instant.now();
        Instant oneDayAgo = now.minus(24, ChronoUnit.HOURS);
        Instant sevenDaysAgo = now.minus(7, ChronoUnit.DAYS);
        Instant thirtyDaysAgo = now.minus(30, ChronoUnit.DAYS);

        long dau = userRepository.countActiveUsersSince(oneDayAgo);
        long wau = userRepository.countActiveUsersSince(sevenDaysAgo);
        long mau = userRepository.countActiveUsersSince(thirtyDaysAgo);

        // Biểu đồ lượt sinh prompt trong 30 ngày gần nhất
        List<Object[]> promptsPerDayRaw = generatedPromptRepository.countPromptsPerDaySince(thirtyDaysAgo);
        Map<String, Long> promptsGeneratedPerDay = new LinkedHashMap<>();

        // Điền trước các ngày bằng 0 để tránh khoảng trống dữ liệu trong biểu đồ
        LocalDate startDay = LocalDate.now(ZoneOffset.UTC).minusDays(30);
        for (int i = 0; i <= 30; i++) {
            promptsGeneratedPerDay.put(startDay.plusDays(i).toString(), 0L);
        }

        for (Object[] row : promptsPerDayRaw) {
            if (row != null && row.length >= 2 && row[0] != null) {
                String dayStr = row[0].toString();
                Long count = ((Number) row[1]).longValue();
                promptsGeneratedPerDay.put(dayStr, count);
            }
        }

        // Top 5 templates phổ biến nhất
        List<Template> popularTemplatesRaw = templateRepository.findAll(
                PageRequest.of(0, 5, Sort.by(Sort.Order.desc("usageCount"), Sort.Order.desc("id")))
        ).getContent();
        List<AnalyticsDashboardResponse.PopularTemplateItem> popularTemplates = popularTemplatesRaw.stream()
                .map(t -> AnalyticsDashboardResponse.PopularTemplateItem.builder()
                        .templateId(t.getId())
                        .slug(t.getSlug())
                        .titleI18n(t.getTitleI18n())
                        .usageCount(t.getUsageCount())
                        .build())
                .collect(Collectors.toList());

        // Top AI models được dùng nhiều nhất
        List<Object[]> mostUsedModelsRaw = generatedPromptRepository.countMostUsedModels(5);
        List<AnalyticsDashboardResponse.MostUsedModelItem> mostUsedModels = new ArrayList<>();
        for (Object[] row : mostUsedModelsRaw) {
            if (row != null && row.length >= 3 && row[0] != null) {
                mostUsedModels.add(AnalyticsDashboardResponse.MostUsedModelItem.builder()
                        .modelId((UUID) row[0])
                        .name((String) row[1])
                        .usageCount(((Number) row[2]).longValue())
                        .build());
            }
        }

        // Phễu chuyển đổi
        long signups = userRepository.count();
        long verifiedEmails = userRepository.countVerifiedUsers();
        long promptGenerations = generatedPromptRepository.countDistinctUsersWithGenerations();

        AnalyticsDashboardResponse response = AnalyticsDashboardResponse.builder()
                .dau(dau)
                .wau(wau)
                .mau(mau)
                .promptsGeneratedPerDay(promptsGeneratedPerDay)
                .popularTemplates(popularTemplates)
                .mostUsedModels(mostUsedModels)
                .conversionFunnel(AnalyticsDashboardResponse.ConversionFunnel.builder()
                        .signups(signups)
                        .verifiedEmails(verifiedEmails)
                        .promptGenerations(promptGenerations)
                        .build())
                .build();

        // 3. Ghi vào cache
        try {
            String json = objectMapper.writeValueAsString(response);
            redis.opsForValue().set(CACHE_KEY, json, CACHE_TTL);
        } catch (Exception e) {
            log.error("Failed to write analytics dashboard to Redis cache: {}", e.getMessage(), e);
        }

        return response;
    }
}
