package com.example.how2prompt.common.exception;

import com.example.how2prompt.common.response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Map mọi exception thành envelope {@link ErrorResponse} thống nhất.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // -------------------------------------------------------------------------
    // Domain
    // -------------------------------------------------------------------------

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException ex) {
        if (ex.getStatus().is5xxServerError()) {
            log.error("AppException [{}]: {}", ex.getCode(), ex.getMessage(), ex);
        } else {
            log.warn("AppException [{}]: {}", ex.getCode(), ex.getMessage());
        }
        return build(ex.getStatus(), ex.getCode(), ex.getMessage(), ex.getDetails());
    }

    // -------------------------------------------------------------------------
    // Validation / binding
    // -------------------------------------------------------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, Object> details = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid",
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
        return build(ErrorCode.VALIDATION_FAILED, details);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex) {
        Map<String, Object> details = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid",
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
        return build(ErrorCode.VALIDATION_FAILED, details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, Object> details = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        v -> v.getMessage() != null ? v.getMessage() : "invalid",
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
        return build(ErrorCode.VALIDATION_FAILED, details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {
        log.debug("Malformed request body: {}", ex.getMessage());
        return build(ErrorCode.MALFORMED_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        Map<String, Object> details = Map.of(
                "parameter", ex.getParameterName(),
                "expectedType", ex.getParameterType()
        );
        return build(
                ErrorCode.BAD_REQUEST,
                "Thiếu tham số bắt buộc: " + ex.getParameterName(),
                details
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("parameter", ex.getName());
        if (ex.getRequiredType() != null) {
            details.put("expectedType", ex.getRequiredType().getSimpleName());
        }
        if (ex.getValue() != null) {
            details.put("value", ex.getValue().toString());
        }
        return build(ErrorCode.INVALID_ARGUMENT, "Kiểu tham số không hợp lệ.", details);
    }

    // -------------------------------------------------------------------------
    // HTTP / routing
    // -------------------------------------------------------------------------

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex) {
        return build(ErrorCode.NOT_FOUND, "Không tìm thấy endpoint.", Map.of("path", ex.getResourcePath()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("method", ex.getMethod());
        if (ex.getSupportedHttpMethods() != null) {
            details.put("supported", ex.getSupportedHttpMethods().stream().map(Object::toString).toList());
        }
        return build(ErrorCode.METHOD_NOT_ALLOWED, details);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaType(HttpMediaTypeNotSupportedException ex) {
        Map<String, Object> details = new LinkedHashMap<>();
        if (ex.getContentType() != null) {
            details.put("contentType", ex.getContentType().toString());
        }
        details.put("supported", ex.getSupportedMediaTypes().stream().map(Object::toString).toList());
        return build(ErrorCode.UNSUPPORTED_MEDIA_TYPE, details);
    }

    // -------------------------------------------------------------------------
    // Security (khi exception ném trong controller / method security)
    // -------------------------------------------------------------------------

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return build(ErrorCode.INVALID_CREDENTIALS);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex) {
        return build(ErrorCode.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return build(ErrorCode.FORBIDDEN);
    }

    // -------------------------------------------------------------------------
    // Persistence
    // -------------------------------------------------------------------------

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        return build(ErrorCode.CONFLICT, "Dữ liệu xung đột hoặc vi phạm ràng buộc.", Map.of());
    }

    // -------------------------------------------------------------------------
    // Fallback
    // -------------------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        return build(ErrorCode.INTERNAL_ERROR);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private ResponseEntity<ErrorResponse> build(ErrorCode code) {
        return build(code.getStatus(), code.getCode(), code.getDefaultMessage(), Map.of());
    }

    private ResponseEntity<ErrorResponse> build(ErrorCode code, Map<String, Object> details) {
        return build(code.getStatus(), code.getCode(), code.getDefaultMessage(), details);
    }

    private ResponseEntity<ErrorResponse> build(ErrorCode code, String message, Map<String, Object> details) {
        return build(code.getStatus(), code.getCode(), message, details);
    }

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status,
            String code,
            String message,
            Map<String, Object> details
    ) {
        return ResponseEntity.status(status).body(ErrorResponse.of(code, message, details));
    }
}
