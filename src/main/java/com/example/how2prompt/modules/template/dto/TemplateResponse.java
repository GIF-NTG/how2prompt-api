package com.example.how2prompt.modules.template.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record TemplateResponse(
        UUID id,
        UUID workspaceId,
        String slug,
        Map<String, Object> titleI18n,
        Map<String, Object> descriptionI18n,
        String coverImage,
        UUID authorId,
        String authorType,
        boolean official,
        boolean isPublic,
        String status,
        UUID currentVersionId,
        long usageCount,
        int favoriteCount,
        Instant featuredAt,
        Instant publishedAt,
        Instant createdAt,
        Instant updatedAt,
        List<UUID> categoryIds,
        List<UUID> tagIds,
        List<TemplateModelItem> models
) {
    public record TemplateModelItem(UUID aiModelId, boolean primary) {
    }
}
