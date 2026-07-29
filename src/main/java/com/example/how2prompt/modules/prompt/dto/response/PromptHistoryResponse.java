package com.example.how2prompt.modules.prompt.dto.response;

import com.example.how2prompt.modules.prompt.entity.GeneratedPrompt;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO tóm tắt cho danh sách lịch sử generate (US-4.2).
 * Chỉ chứa thông tin cần thiết cho list view — chi tiết dùng
 * {@code GeneratePromptResponse} (trong module template).
 */
public record PromptHistoryResponse(
        UUID id,
        UUID templateId,
        UUID templateVersionId,
        UUID aiModelId,
        String title,
        String finalPromptPreview,
        Instant createdAt
) {

    private static final int PREVIEW_MAX_LENGTH = 200;

    public static PromptHistoryResponse from(GeneratedPrompt entity) {
        return new PromptHistoryResponse(
                entity.getId(),
                entity.getTemplateId(),
                entity.getTemplateVersionId(),
                entity.getAiModelId(),
                entity.getTitle(),
                truncate(entity.getFinalPrompt()),
                entity.getCreatedAt()
        );
    }

    private static String truncate(String text) {
        if (text == null) {
            return null;
        }
        return text.length() <= PREVIEW_MAX_LENGTH
                ? text
                : text.substring(0, PREVIEW_MAX_LENGTH) + "…";
    }
}
