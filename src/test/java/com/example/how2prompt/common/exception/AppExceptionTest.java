package com.example.how2prompt.common.exception;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

class AppExceptionTest {

    @Test
    void testAppException() {
        AppException ex = new AppException(ErrorCode.INTERNAL_ERROR, "Test Error");
        assertEquals(ErrorCode.INTERNAL_ERROR, ex.getErrorCode());
        assertEquals(ErrorCode.INTERNAL_ERROR.getCode(), ex.getCode());
        assertEquals(ErrorCode.INTERNAL_ERROR.getStatus(), ex.getStatus());
        assertEquals("Test Error", ex.getMessage());
        
        AppException ex2 = new AppException(ErrorCode.BAD_REQUEST);
        assertEquals(ErrorCode.BAD_REQUEST.getDefaultMessage(), ex2.getMessage());
        
        AppException ex3 = new AppException(ErrorCode.BAD_REQUEST, (String) null);
        assertEquals(ErrorCode.BAD_REQUEST.getDefaultMessage(), ex3.getMessage());
        
        AppException ex4 = new AppException(ErrorCode.BAD_REQUEST, Map.of("key", "value"));
        assertEquals("value", ex4.getDetails().get("key"));
        
        AppException ex5 = new AppException(ErrorCode.BAD_REQUEST, "Msg", new RuntimeException());
        assertEquals("Msg", ex5.getMessage());
        assertNotNull(ex5.getCause());
        
        AppException ex6 = new AppException(ErrorCode.BAD_REQUEST, null, new RuntimeException());
        assertEquals(ErrorCode.BAD_REQUEST.getDefaultMessage(), ex6.getMessage());
        
        AppException ex7 = new AppException(ErrorCode.BAD_REQUEST, "Msg", (Map<String, Object>) null);
        assertTrue(ex7.getDetails().isEmpty());
    }

    @Test
    void testBadRequestException() {
        BadRequestException ex = new BadRequestException("Bad request error");
        assertEquals(ErrorCode.BAD_REQUEST, ex.getErrorCode());
        assertEquals("Bad request error", ex.getMessage());
    }

    @Test
    void testBusinessException() {
        BusinessException ex = new BusinessException("Business logic error");
        assertEquals(ErrorCode.BUSINESS_RULE_VIOLATION, ex.getErrorCode());
        assertEquals("Business logic error", ex.getMessage());
        
        BusinessException ex2 = new BusinessException(ErrorCode.INVALID_ARGUMENT, "Custom business error");
        assertEquals(ErrorCode.INVALID_ARGUMENT, ex2.getErrorCode());
        assertEquals("Custom business error", ex2.getMessage());
    }

    @Test
    void testConflictException() {
        ConflictException ex = new ConflictException("Resource conflict");
        assertEquals(ErrorCode.CONFLICT, ex.getErrorCode());
        assertEquals("Resource conflict", ex.getMessage());
        
        ConflictException ex2 = new ConflictException("Msg", Map.of("k", "v"));
        assertEquals("v", ex2.getDetails().get("k"));
        
        ConflictException ex3 = ConflictException.alreadyExists("User", "email", "test@test.com");
        assertEquals("User đã tồn tại.", ex3.getMessage());
        assertEquals("test@test.com", ex3.getDetails().get("value"));
        
        ConflictException ex4 = ConflictException.alreadyExists("User", "email", null);
        assertNull(ex4.getDetails().get("value"));
    }

    @Test
    void testForbiddenException() {
        ForbiddenException ex = new ForbiddenException("Access denied");
        assertEquals(ErrorCode.FORBIDDEN, ex.getErrorCode());
        assertEquals("Access denied", ex.getMessage());
    }

    @Test
    void testResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Entity", "id123");
        assertEquals(ErrorCode.NOT_FOUND, ex.getErrorCode());
        assertEquals("Entity không tồn tại.", ex.getMessage());
        
        ResourceNotFoundException ex2 = ResourceNotFoundException.of("Entity", null);
        assertNull(ex2.getDetails().get("id"));
        
        ResourceNotFoundException ex3 = new ResourceNotFoundException("Custom message");
        assertEquals("Custom message", ex3.getMessage());
    }

    @Test
    void testUnauthorizedException() {
        UnauthorizedException ex = new UnauthorizedException("Unauthorized access");
        assertEquals(ErrorCode.UNAUTHORIZED, ex.getErrorCode());
        assertEquals("Unauthorized access", ex.getMessage());
    }
}
