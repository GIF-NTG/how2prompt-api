package com.example.how2prompt.modules.template.dto;

import java.util.Map;
import java.util.UUID;

/**
 * Kết quả render prompt (DTO thuần — không expose entity).
 * Public contract cho module prompt / generate API.
 */
public record RenderResult(
        UUID templateId,
        UUID templateVersionId,
        UUID aiModelId,
        String renderedPrompt,
        String systemPrompt,
        boolean usedVariant,
        Map<String, Object> resolvedInputValues,
        String extraInstructions
) {
}
