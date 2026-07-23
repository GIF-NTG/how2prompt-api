package com.example.how2prompt.common.exception;

import java.util.Map;

/**
 * 400 — request sai / không xử lý được (không phải validation field-level).
 */
public class BadRequestException extends AppException {

    public BadRequestException(String message) {
        super(ErrorCode.BAD_REQUEST, message);
    }

    public BadRequestException(String message, Map<String, Object> details) {
        super(ErrorCode.BAD_REQUEST, message, details);
    }

    public BadRequestException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
