package com.example.how2prompt.common.exception;

import java.util.Map;

/**
 * 403 — đã xác thực nhưng không đủ quyền.
 */
public class ForbiddenException extends AppException {

    public ForbiddenException() {
        super(ErrorCode.FORBIDDEN);
    }

    public ForbiddenException(String message) {
        super(ErrorCode.FORBIDDEN, message);
    }

    public ForbiddenException(String message, Map<String, Object> details) {
        super(ErrorCode.FORBIDDEN, message, details);
    }
}
