package com.example.how2prompt.common.exception;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 404 — tài nguyên không tồn tại (hoặc soft-deleted, tuỳ service).
 */
public class ResourceNotFoundException extends AppException {

    public ResourceNotFoundException(String message) {
        super(ErrorCode.NOT_FOUND, message);
    }

    public ResourceNotFoundException(String resource, Object id) {
        super(
                ErrorCode.NOT_FOUND,
                resource + " không tồn tại.",
                details(resource, id)
        );
    }

    public static ResourceNotFoundException of(String resource, Object id) {
        return new ResourceNotFoundException(resource, id);
    }

    private static Map<String, Object> details(String resource, Object id) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("resource", resource);
        if (id != null) {
            map.put("id", id.toString());
        }
        return map;
    }
}
