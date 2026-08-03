package com.example.how2prompt.modules.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsDashboardResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private long dau;
    private long wau;
    private long mau;

    private Map<String, Long> promptsGeneratedPerDay;

    private List<PopularTemplateItem> popularTemplates;
    private List<MostUsedModelItem> mostUsedModels;

    private ConversionFunnel conversionFunnel;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PopularTemplateItem implements Serializable {
        private static final long serialVersionUID = 1L;
        private UUID templateId;
        private String slug;
        private Map<String, Object> titleI18n;
        private long usageCount;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MostUsedModelItem implements Serializable {
        private static final long serialVersionUID = 1L;
        private UUID modelId;
        private String name;
        private long usageCount;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConversionFunnel implements Serializable {
        private static final long serialVersionUID = 1L;
        private long signups;
        private long verifiedEmails;
        private long promptGenerations;
    }
}
