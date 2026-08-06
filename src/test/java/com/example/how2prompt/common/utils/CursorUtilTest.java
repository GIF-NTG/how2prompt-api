package com.example.how2prompt.common.utils;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CursorUtilTest {

    @Test
    void encode_NullValues() {
        assertNull(CursorUtil.encode(null, UUID.randomUUID()));
        assertNull(CursorUtil.encode("value", null));
    }

    @Test
    void encode_InstantSortValue() {
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        String encoded = CursorUtil.encode(now, id);
        assertNotNull(encoded);
    }

    @Test
    void encode_NonInstantSortValue() {
        Long sortVal = 100L;
        UUID id = UUID.randomUUID();
        String encoded = CursorUtil.encode(sortVal, id);
        assertNotNull(encoded);
    }

    @Test
    void decode_NullOrBlank() {
        assertNull(CursorUtil.decode(null, "newest"));
        assertNull(CursorUtil.decode("  ", "newest"));
    }

    @Test
    void decode_InvalidBase64() {
        assertNull(CursorUtil.decode("invalid_base64!!!", "newest"));
    }

    @Test
    void decode_NoSeparator() {
        String encoded = java.util.Base64.getEncoder().encodeToString("noseparator".getBytes());
        assertNull(CursorUtil.decode(encoded, "newest"));
    }

    @Test
    void decode_TrendingSortValue() {
        UUID id = UUID.randomUUID();
        String encoded = CursorUtil.encode(100L, id);
        CursorUtil.DecodedCursor decoded = CursorUtil.decode(encoded, "trending");
        
        assertNotNull(decoded);
        assertEquals(100L, decoded.getSortValue());
        assertEquals(id, decoded.getId());
    }

    @Test
    void decode_PopularSortValue() {
        UUID id = UUID.randomUUID();
        String encoded = CursorUtil.encode(100L, id);
        CursorUtil.DecodedCursor decoded = CursorUtil.decode(encoded, "popular");
        
        assertNotNull(decoded);
        assertEquals(100L, decoded.getSortValue());
        assertEquals(id, decoded.getId());
    }

    @Test
    void decode_InstantSortValue() {
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        String encoded = CursorUtil.encode(now, id);
        CursorUtil.DecodedCursor decoded = CursorUtil.decode(encoded, "newest");
        
        assertNotNull(decoded);
        assertEquals(now, decoded.getSortValue());
        assertEquals(id, decoded.getId());
    }

    @Test
    void decode_InvalidNumberFormat() {
        UUID id = UUID.randomUUID();
        // Cố tình encode một string không phải số, nhưng lại gọi decode "trending" (cần số)
        String encoded = CursorUtil.encode("not_a_number", id);
        assertNull(CursorUtil.decode(encoded, "trending"));
    }
}
