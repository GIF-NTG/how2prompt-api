package com.example.how2prompt.modules.template.dto;

import java.util.Map;
import java.util.UUID;

/**
 * Response generate (US-3.6). History được xử lý riêng và không nằm trong
 * response; client dùng trực tiếp {@code finalPrompt}.
 */
public record GeneratePromptResponse(
        UUID templateId,
        UUID templateVersionId,
        UUID aiModelId,
        String finalPrompt,
        String systemPrompt,
        boolean usedVariant,
        Map<String, Object> resolvedInputValues,
        String extraInstructions,
        String title
) {
    public static GeneratePromptResponse from(RenderResult render, String title) {
        return new GeneratePromptResponse(
                render.templateId(),
                render.templateVersionId(),
                render.aiModelId(),
                render.renderedPrompt(),
                render.systemPrompt(),
                render.usedVariant(),
                render.resolvedInputValues(),
                render.extraInstructions(),
                title
        );
    }
}
