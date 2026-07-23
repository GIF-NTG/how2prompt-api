package com.example.how2prompt.common.exception;

import java.util.Map;

/**
 * 401 — chưa xác thực / token lỗi. Thường dùng trong service layer;
 * filter Security vẫn trả cùng envelope qua {@code SecurityConfig}.
 */
public class UnauthorizedException extends AppException {

    public UnauthorizedException() {
        super(ErrorCode.UNAUTHORIZED);
    }

    public UnauthorizedException(String message) {
        super(ErrorCode.UNAUTHORIZED, message);
    }

    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }

    public UnauthorizedException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public UnauthorizedException(ErrorCode errorCode, String message, Map<String, Object> details) {
        super(errorCode, message, details);
    }
}
