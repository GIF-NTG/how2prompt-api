package com.example.how2prompt.common.exception;

import java.util.Map;

/**
 * 422 — vi phạm quy tắc nghiệp vụ (request hợp lệ về cú pháp nhưng không thỏa rule).
 */
public class BusinessException extends AppException {

    public BusinessException(String message) {
        super(ErrorCode.BUSINESS_RULE_VIOLATION, message);
    }

    public BusinessException(String message, Map<String, Object> details) {
        super(ErrorCode.BUSINESS_RULE_VIOLATION, message, details);
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public BusinessException(ErrorCode errorCode, String message, Map<String, Object> details) {
        super(errorCode, message, details);
    }
}
