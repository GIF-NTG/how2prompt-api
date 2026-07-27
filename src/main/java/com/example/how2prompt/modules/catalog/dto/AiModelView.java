package com.example.how2prompt.modules.catalog.dto;

import java.util.UUID;

/**
 * Read-model public của module catalog — module khác không phụ thuộc entity {@code AiModel}.
 */
public record AiModelView(
        UUID id,
        String code,
        String name,
        String provider,
        String modelType,
        boolean active
) {
}
