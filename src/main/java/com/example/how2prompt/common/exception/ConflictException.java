package com.example.how2prompt.common.exception;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 409 — trùng unique / trạng thái xung đột.
 */
public class ConflictException extends AppException {

    public ConflictException(String message) {
        super(ErrorCode.CONFLICT, message);
    }

    public ConflictException(String message, Map<String, Object> details) {
        super(ErrorCode.CONFLICT, message, details);
    }

    public static ConflictException alreadyExists(String resource, String field, Object value) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("resource", resource);
        details.put("field", field);
        if (value != null) {
            details.put("value", value.toString());
        }
        return new ConflictException(resource + " đã tồn tại.", details);
    }
}
