package com.example.how2prompt.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Mã lỗi nghiệp vụ / hệ thống dùng chung. Message mặc định tiếng Việt (user-facing).
 * Controller/service có thể override message khi throw {@link AppException}.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // --- Auth / Security ---
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Yêu cầu chưa xác thực hoặc token không hợp lệ."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện hành động này."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không đúng."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Token đã hết hạn."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "Token không hợp lệ."),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "Email chưa được xác minh."),

    // --- Validation / Request ---
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Yêu cầu không hợp lệ."),
    INVALID_ARGUMENT(HttpStatus.BAD_REQUEST, "Tham số không hợp lệ."),
    MALFORMED_REQUEST(HttpStatus.BAD_REQUEST, "Định dạng request không hợp lệ."),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content-Type không được hỗ trợ."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "Phương thức HTTP không được phép."),

    // --- Resource ---
    NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy tài nguyên."),
    CONFLICT(HttpStatus.CONFLICT, "Tài nguyên đã tồn tại hoặc xung đột."),
    ALREADY_EXISTS(HttpStatus.CONFLICT, "Tài nguyên đã tồn tại."),
    GONE(HttpStatus.GONE, "Tài nguyên đã bị xoá hoặc không còn khả dụng."),

    // --- Business ---
    BUSINESS_RULE_VIOLATION(HttpStatus.UNPROCESSABLE_ENTITY, "Vi phạm quy tắc nghiệp vụ."),
    RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "Quá nhiều yêu cầu. Vui lòng thử lại sau."),

    // --- System ---
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống. Vui lòng thử lại sau."),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "Dịch vụ tạm thời không khả dụng.");

    private final HttpStatus status;
    private final String defaultMessage;

    public String getCode() {
        return name();
    }
}
