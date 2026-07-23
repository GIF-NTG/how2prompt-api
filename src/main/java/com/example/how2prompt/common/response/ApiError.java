package com.example.how2prompt.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Collections;
import java.util.Map;

/**
 * Payload lỗi chuẩn — khớp envelope Security / GlobalExceptionHandler:
 * {@code { "error": { "code", "message", "details" } }}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        String code,
        String message,
        Map<String, Object> details
) {

    public ApiError {
        details = details == null || details.isEmpty()
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(details);
    }

    public static ApiError of(String code, String message) {
        return new ApiError(code, message, Map.of());
    }

    public static ApiError of(String code, String message, Map<String, Object> details) {
        return new ApiError(code, message, details);
    }
}
