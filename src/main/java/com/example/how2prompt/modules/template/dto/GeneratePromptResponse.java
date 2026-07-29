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
        UUID generatedPromptId,
        String finalPrompt,
        String systemPrompt,
        boolean usedVariant,
        Map<String, Object> resolvedInputValues,
        String extraInstructions,
        String title,
        Integer tokensEstimate
) {
    public static GeneratePromptResponse from(RenderResult render, UUID generatedPromptId, String title) {
        int totalCharCount = (render.renderedPrompt() != null ? render.renderedPrompt().length() : 0)
                + (render.systemPrompt() != null ? render.systemPrompt().length() : 0);
        int tokensEstimate = (int) Math.ceil(totalCharCount / 4.0);

        return new GeneratePromptResponse(
                render.templateId(),
                render.templateVersionId(),
                render.aiModelId(),
                generatedPromptId,
                render.renderedPrompt(),
                render.systemPrompt(),
                render.usedVariant(),
                render.resolvedInputValues(),
                render.extraInstructions(),
                title,
                tokensEstimate
        );
    }
}
