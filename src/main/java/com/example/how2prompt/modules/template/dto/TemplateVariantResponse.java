package com.example.how2prompt.modules.template.dto;

import java.util.Map;
import java.util.UUID;

public record TemplateVariantResponse(
        UUID id,
        UUID templateVersionId,
        UUID aiModelId,
        String promptBodyOverride,
        String systemPromptOverride,
        Map<String, Object> modelConfig,
        Map<String, Object> notesI18n
) {
}
