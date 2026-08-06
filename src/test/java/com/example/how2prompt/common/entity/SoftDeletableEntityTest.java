package com.example.how2prompt.common.entity;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SoftDeletableEntityTest {

    private static class DummySoftEntity extends SoftDeletableEntity {}

    @Test
    void testGettersAndSetters() {
        DummySoftEntity entity = new DummySoftEntity();
        
        UUID id = UUID.randomUUID();
        entity.setId(id);
        
        Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setDeletedAt(now);
        
        assertEquals(id, entity.getId());
        assertEquals(now, entity.getCreatedAt());
        assertEquals(now, entity.getUpdatedAt());
        assertEquals(now, entity.getDeletedAt());
    }
}
