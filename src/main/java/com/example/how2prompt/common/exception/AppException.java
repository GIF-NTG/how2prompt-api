package com.example.how2prompt.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.Map;

/**
 * Exception nghiệp vụ gốc. Mọi lỗi domain nên throw subclass hoặc {@code AppException}
 * với {@link ErrorCode} — {@link GlobalExceptionHandler} map sang {@code ErrorResponse}.
 */
@Getter
public class AppException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Map<String, Object> details;

    public AppException(ErrorCode errorCode) {
        this(errorCode, errorCode.getDefaultMessage(), Map.of());
    }

    public AppException(ErrorCode errorCode, String message) {
        this(errorCode, message, Map.of());
    }

    public AppException(ErrorCode errorCode, Map<String, Object> details) {
        this(errorCode, errorCode.getDefaultMessage(), details);
    }

    public AppException(ErrorCode errorCode, String message, Map<String, Object> details) {
        super(message != null ? message : errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.details = details == null || details.isEmpty()
                ? Map.of()
                : Collections.unmodifiableMap(details);
    }

    public AppException(ErrorCode errorCode, String message, Throwable cause) {
        super(message != null ? message : errorCode.getDefaultMessage(), cause);
        this.errorCode = errorCode;
        this.details = Map.of();
    }

    public HttpStatus getStatus() {
        return errorCode.getStatus();
    }

    public String getCode() {
        return errorCode.getCode();
    }
}
