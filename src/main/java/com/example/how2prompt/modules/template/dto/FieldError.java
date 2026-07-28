package com.example.how2prompt.modules.template.dto;

/**
 * Lỗi validation một field của template form (gom trong {@code details.fields}).
 */
public record FieldError(
        String field,
        String code,
        String message
) {
}
