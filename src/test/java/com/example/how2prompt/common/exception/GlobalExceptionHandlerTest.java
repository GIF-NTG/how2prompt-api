package com.example.how2prompt.common.exception;

import com.example.how2prompt.common.response.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void testAppException_5xx() {
        AppException ex = new AppException(ErrorCode.INTERNAL_ERROR, "Server Error");
        ResponseEntity<ErrorResponse> res = handler.handleAppException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, res.getStatusCode());
        assertEquals(ErrorCode.INTERNAL_ERROR.getCode(), res.getBody().error().code());
        assertEquals("Server Error", res.getBody().error().message());
    }

    @Test
    void testAppException_4xx() {
        AppException ex = new AppException(ErrorCode.BAD_REQUEST, "Bad Request");
        ResponseEntity<ErrorResponse> res = handler.handleAppException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertEquals(ErrorCode.BAD_REQUEST.getCode(), res.getBody().error().code());
        assertEquals("Bad Request", res.getBody().error().message());
    }

    @Test
    void testMethodArgumentNotValidException() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("object", "field1", "must not be null"),
                new FieldError("object", "field2", null)
        ));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(mock(MethodParameter.class), bindingResult);

        ResponseEntity<ErrorResponse> res = handler.handleMethodArgumentNotValid(ex);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertEquals("VALIDATION_FAILED", res.getBody().error().code());
        assertEquals("must not be null", res.getBody().error().details().get("field1"));
        assertEquals("invalid", res.getBody().error().details().get("field2"));
    }

    @Test
    void testBindException() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("object", "field2", "invalid email"),
                new FieldError("object", "field3", null)
        ));
        BindException ex = new BindException(bindingResult);

        ResponseEntity<ErrorResponse> res = handler.handleBindException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertEquals("invalid email", res.getBody().error().details().get("field2"));
        assertEquals("invalid", res.getBody().error().details().get("field3"));
    }

    @Test
    void testConstraintViolationException() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("field3");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must be positive");
        
        ConstraintViolation<?> violationNull = mock(ConstraintViolation.class);
        Path pathNull = mock(Path.class);
        when(pathNull.toString()).thenReturn("field4");
        when(violationNull.getPropertyPath()).thenReturn(pathNull);
        when(violationNull.getMessage()).thenReturn(null);

        ConstraintViolationException ex = new ConstraintViolationException("error", Set.of(violation, violationNull));

        ResponseEntity<ErrorResponse> res = handler.handleConstraintViolation(ex);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertEquals("must be positive", res.getBody().error().details().get("field3"));
        assertEquals("invalid", res.getBody().error().details().get("field4"));
    }

    @Test
    void testHttpMessageNotReadableException() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("JSON parse error", new org.springframework.mock.http.MockHttpInputMessage(new byte[0]));
        ResponseEntity<ErrorResponse> res = handler.handleNotReadable(ex);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertEquals("MALFORMED_REQUEST", res.getBody().error().code());
    }

    @Test
    void testMissingServletRequestParameterException() {
        MissingServletRequestParameterException ex = new MissingServletRequestParameterException("param1", "String");
        ResponseEntity<ErrorResponse> res = handler.handleMissingParam(ex);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertEquals("param1", res.getBody().error().details().get("parameter"));
    }

    @Test
    void testMethodArgumentTypeMismatchException() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException("abc", Integer.class, "param2", mock(MethodParameter.class), new IllegalArgumentException());
        ResponseEntity<ErrorResponse> res = handler.handleTypeMismatch(ex);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertEquals("param2", res.getBody().error().details().get("parameter"));
        assertEquals("Integer", res.getBody().error().details().get("expectedType"));
        assertEquals("abc", res.getBody().error().details().get("value"));
        
        MethodArgumentTypeMismatchException exNull = new MethodArgumentTypeMismatchException(null, null, "param3", mock(MethodParameter.class), new IllegalArgumentException());
        ResponseEntity<ErrorResponse> resNull = handler.handleTypeMismatch(exNull);
        assertEquals(HttpStatus.BAD_REQUEST, resNull.getStatusCode());
        assertEquals("param3", resNull.getBody().error().details().get("parameter"));
        assertNull(resNull.getBody().error().details().get("expectedType"));
        assertNull(resNull.getBody().error().details().get("value"));
    }

    @Test
    void testNoResourceFoundException() {
        NoResourceFoundException ex = mock(NoResourceFoundException.class);
        when(ex.getResourcePath()).thenReturn("/api/test");
        ResponseEntity<ErrorResponse> res = handler.handleNoResource(ex);
        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
        assertEquals("/api/test", res.getBody().error().details().get("path"));
    }

    @Test
    void testHttpRequestMethodNotSupportedException() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("POST", List.of("GET"));
        ResponseEntity<ErrorResponse> res = handler.handleMethodNotAllowed(ex);
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, res.getStatusCode());
        assertEquals("POST", res.getBody().error().details().get("method"));
        
        HttpRequestMethodNotSupportedException exNull = new HttpRequestMethodNotSupportedException("PUT");
        ResponseEntity<ErrorResponse> resNull = handler.handleMethodNotAllowed(exNull);
        assertEquals("PUT", resNull.getBody().error().details().get("method"));
        assertNull(resNull.getBody().error().details().get("supported"));
    }

    @Test
    void testHttpMediaTypeNotSupportedException() {
        HttpMediaTypeNotSupportedException ex = new HttpMediaTypeNotSupportedException(MediaType.APPLICATION_XML, List.of(MediaType.APPLICATION_JSON));
        ResponseEntity<ErrorResponse> res = handler.handleMediaType(ex);
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, res.getStatusCode());
        assertEquals("application/xml", res.getBody().error().details().get("contentType"));
        
        HttpMediaTypeNotSupportedException exNull = new HttpMediaTypeNotSupportedException((MediaType) null, List.of(MediaType.APPLICATION_JSON));
        ResponseEntity<ErrorResponse> resNull = handler.handleMediaType(exNull);
        assertNull(resNull.getBody().error().details().get("contentType"));
    }

    @Test
    void testBadCredentialsException() {
        BadCredentialsException ex = new BadCredentialsException("Bad creds");
        ResponseEntity<ErrorResponse> res = handler.handleBadCredentials(ex);
        assertEquals(HttpStatus.UNAUTHORIZED, res.getStatusCode());
        assertEquals("INVALID_CREDENTIALS", res.getBody().error().code());
    }

    @Test
    void testAuthenticationException() {
        AuthenticationException ex = new AuthenticationException("Unauthorized") {};
        ResponseEntity<ErrorResponse> res = handler.handleAuthentication(ex);
        assertEquals(HttpStatus.UNAUTHORIZED, res.getStatusCode());
    }

    @Test
    void testAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Forbidden");
        ResponseEntity<ErrorResponse> res = handler.handleAccessDenied(ex);
        assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
    }

    @Test
    void testDataIntegrityViolationException() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Constraint violation", new RuntimeException("duplicate key"));
        ResponseEntity<ErrorResponse> res = handler.handleDataIntegrity(ex);
        assertEquals(HttpStatus.CONFLICT, res.getStatusCode());
    }

    @Test
    void testException() {
        Exception ex = new Exception("Unknown error");
        ResponseEntity<ErrorResponse> res = handler.handleUnexpected(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, res.getStatusCode());
    }
}
