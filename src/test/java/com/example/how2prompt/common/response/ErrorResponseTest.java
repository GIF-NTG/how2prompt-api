package com.example.how2prompt.common.response;

import org.junit.jupiter.api.Test;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ErrorResponseTest {

    @Test
    void testOf() {
        ErrorResponse res1 = ErrorResponse.of("CODE", "Msg");
        assertNotNull(res1.error());
        assertEquals("CODE", res1.error().code());
        assertEquals("Msg", res1.error().message());
        
        ErrorResponse res2 = ErrorResponse.of("CODE2", "Msg2", Map.of("key", "val"));
        assertNotNull(res2.error());
        assertEquals("CODE2", res2.error().code());
        assertEquals("val", res2.error().details().get("key"));
        
        ErrorResponse res3 = ErrorResponse.of(ApiError.of("CODE3", "Msg3"));
        assertNotNull(res3.error());
        assertEquals("CODE3", res3.error().code());
    }
}
