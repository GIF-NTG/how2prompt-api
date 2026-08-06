package com.example.how2prompt.common.entity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BaseEntityTest {

    private static class DummyEntity extends BaseEntity {}

    @Test
    void testEqualsAndHashCode() {
        DummyEntity e1 = new DummyEntity();
        UUID id1 = UUID.randomUUID();
        e1.setId(id1);

        DummyEntity e2 = new DummyEntity();
        e2.setId(id1);

        DummyEntity e3 = new DummyEntity();
        e3.setId(UUID.randomUUID());

        // Same object
        assertTrue(e1.equals(e1));
        assertEquals(e1.hashCode(), e1.hashCode());

        // Same ID
        assertTrue(e1.equals(e2));
        
        // Different ID
        assertFalse(e1.equals(e3));
        
        // Null or different class
        assertFalse(e1.equals(null));
        assertFalse(e1.equals(new Object()));

        // Null IDs
        DummyEntity e4 = new DummyEntity();
        DummyEntity e5 = new DummyEntity();
        assertFalse(e4.equals(e5));
        
        // Ensure hashCode is consistent with class
        assertEquals(e1.getClass().hashCode(), e1.hashCode());
    }
}
