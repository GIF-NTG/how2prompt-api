package com.example.how2prompt.common.response;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageResponseTest {

    @Test
    void testPageResponse() {
        PageResponse<String> res = new PageResponse<>();
        res.setItems(Collections.singletonList("item"));
        res.setNextCursor("cursor");
        res.setHasMore(true);
        
        assertEquals(1, res.getItems().size());
        assertEquals("item", res.getItems().get(0));
        assertEquals("cursor", res.getNextCursor());
        assertTrue(res.isHasMore());
        
        PageResponse<String> res2 = new PageResponse<>(Collections.singletonList("item2"), "cursor2", false);
        assertEquals(1, res2.getItems().size());
        assertEquals("item2", res2.getItems().get(0));
        assertEquals("cursor2", res2.getNextCursor());
        assertFalse(res2.isHasMore());
    }
}
