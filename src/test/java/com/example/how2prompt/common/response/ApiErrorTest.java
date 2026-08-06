package com.example.how2prompt.common.response;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiErrorTest {

    @Test
    void testApiError() {
        ApiError error = ApiError.of("CODE", "Message");
        assertEquals("CODE", error.code());
        assertEquals("Message", error.message());
        assertTrue(error.details().isEmpty());
        
        ApiError error2 = ApiError.of("NEW_CODE", "New Message", Map.of("key", "value"));
        assertEquals("NEW_CODE", error2.code());
        assertEquals("New Message", error2.message());
        assertEquals("value", error2.details().get("key"));
        
        ApiError error3 = new ApiError("CODE3", "Msg3", null);
        assertTrue(error3.details().isEmpty());
    }
}
