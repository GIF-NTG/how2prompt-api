package com.example.how2prompt.modules.identity.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class WorkspaceTypeConverterTest {

    private final WorkspaceTypeConverter converter = new WorkspaceTypeConverter();

    @Test
    void convertToDatabaseColumn_ValidType() {
        assertEquals("personal", converter.convertToDatabaseColumn(WorkspaceType.PERSONAL));
        assertEquals("team", converter.convertToDatabaseColumn(WorkspaceType.TEAM));
    }

    @Test
    void convertToDatabaseColumn_Null() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToEntityAttribute_ValidString() {
        assertEquals(WorkspaceType.PERSONAL, converter.convertToEntityAttribute("personal"));
        assertEquals(WorkspaceType.TEAM, converter.convertToEntityAttribute("team"));
        assertEquals(WorkspaceType.PERSONAL, converter.convertToEntityAttribute("PERSONAL"));
    }

    @Test
    void convertToEntityAttribute_Null() {
        assertNull(converter.convertToEntityAttribute(null));
    }
}
