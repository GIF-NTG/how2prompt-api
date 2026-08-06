package com.example.how2prompt.common.response;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ApiResponseTest {

    @Test
    void testOf() {
        ApiResponse<String> res = ApiResponse.of("test");
        assertEquals("test", res.data());
        assertNull(res.meta());
    }
    
    @Test
    void testEmpty() {
        ApiResponse<String> res = ApiResponse.empty();
        assertNull(res.data());
        assertNull(res.meta());
    }

    @Test
    void testPageFromSpringPage() {
        Page<String> springPage = new PageImpl<>(Collections.singletonList("test"), PageRequest.of(0, 10), 100);
        ApiResponse<java.util.List<String>> res = ApiResponse.page(springPage);
        
        assertEquals(1, res.data().size());
        assertEquals("test", res.data().get(0));
        assertEquals(0, res.meta().page());
        assertEquals(10, res.meta().size());
    }
    
    @Test
    void testPageFromSpringPageWithMapper() {
        Page<String> springPage = new PageImpl<>(Collections.singletonList("test"), PageRequest.of(0, 10), 100);
        ApiResponse<java.util.List<Integer>> res = ApiResponse.page(springPage, String::length);
        
        assertEquals(1, res.data().size());
        assertEquals(4, res.data().get(0));
        assertEquals(0, res.meta().page());
        assertEquals(10, res.meta().size());
    }
    
    @Test
    void testPageFromListAndMeta() {
        PageMeta meta = new PageMeta(1, 10, 100L, 10, true, false);
        ApiResponse<java.util.List<String>> res = ApiResponse.page(Collections.singletonList("test"), meta);
        
        assertEquals(1, res.data().size());
        assertEquals("test", res.data().get(0));
        assertEquals(1, res.meta().page());
    }
}
