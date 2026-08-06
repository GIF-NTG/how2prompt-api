package com.example.how2prompt.common.response;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageMetaTest {

    @Test
    void testPageMeta() {
        PageMeta meta = new PageMeta(1, 10, 100L, 10, true, false);
        assertEquals(1, meta.page());
        assertEquals(10, meta.size());
        assertEquals(100L, meta.totalElements());
        assertEquals(10, meta.totalPages());
        assertTrue(meta.hasNext());
        assertFalse(meta.hasPrevious());
        
        Page<String> springPage = new PageImpl<>(Collections.singletonList("test"), PageRequest.of(1, 10), 100);
        PageMeta metaFrom = PageMeta.from(springPage);
        assertEquals(1, metaFrom.page());
        assertEquals(10, metaFrom.size());
        assertEquals(100L, metaFrom.totalElements());
        assertEquals(10, metaFrom.totalPages());
        assertTrue(metaFrom.hasNext());
        assertTrue(metaFrom.hasPrevious());
    }
}
