package com.example.how2prompt.common.response;

import java.util.Map;

/**
 * Envelope lỗi HTTP body.
 *
 * <pre>{@code
 * {
 *   "error": {
 *     "code": "NOT_FOUND",
 *     "message": "...",
 *     "details": {}
 *   }
 * }
 * }</pre>
 */
public record ErrorResponse(ApiError error) {

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(ApiError.of(code, message));
    }

    public static ErrorResponse of(String code, String message, Map<String, Object> details) {
        return new ErrorResponse(ApiError.of(code, message, details));
    }

    public static ErrorResponse of(ApiError error) {
        return new ErrorResponse(error);
    }
}
