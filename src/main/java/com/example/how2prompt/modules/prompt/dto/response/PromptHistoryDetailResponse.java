package com.example.how2prompt.modules.prompt.dto.response;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * DTO chi tiết cho việc xem lại và tái điền form của generated prompt (US-4.3).
 */
public record PromptHistoryDetailResponse(
        UUID id,
        UUID templateId,
        UUID templateVersionId,
        UUID aiModelId,
        String title,
        Map<String, Object> inputValues,
        String extraInstructions,
        String finalPrompt,
        Instant createdAt,
        boolean templateDeleted,
        boolean newerVersionAvailable,
        String latestVersionNumber
) {
}
