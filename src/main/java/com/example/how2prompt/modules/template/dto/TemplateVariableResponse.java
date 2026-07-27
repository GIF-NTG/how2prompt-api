package com.example.how2prompt.modules.template.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record TemplateVariableResponse(
        UUID id,
        UUID templateVersionId,
        String varKey,
        Map<String, Object> labelI18n,
        Map<String, Object> descriptionI18n,
        Map<String, Object> placeholderI18n,
        Map<String, Object> helpTextI18n,
        String inputType,
        boolean required,
        String defaultValue,
        List<Object> options,
        Map<String, Object> validation,
        int sortOrder
) {
}
