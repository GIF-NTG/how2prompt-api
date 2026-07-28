package com.example.how2prompt.modules.template.exception;

import com.example.how2prompt.common.exception.AppException;
import com.example.how2prompt.common.exception.ErrorCode;
import com.example.how2prompt.modules.template.dto.FieldError;

import java.util.List;
import java.util.Map;

/**
 * Lỗi validation form template — map ra:
 * <pre>{@code
 * { "error": { "code": "VALIDATION_ERROR", "message": "...", "details": { "fields": [...] } } }
 * }</pre>
 */
public class TemplateValidationException extends AppException {

    private final List<FieldError> fieldErrors;

    public TemplateValidationException(List<FieldError> fieldErrors) {
        super(
                ErrorCode.VALIDATION_ERROR,
                ErrorCode.VALIDATION_ERROR.getDefaultMessage(),
                Map.of("fields", List.copyOf(fieldErrors))
        );
        this.fieldErrors = List.copyOf(fieldErrors);
    }

    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }
}
