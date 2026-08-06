package com.example.how2prompt.modules.identity.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class WorkspaceRoleConverterTest {

    private final WorkspaceRoleConverter converter = new WorkspaceRoleConverter();

    @Test
    void convertToDatabaseColumn_ValidRole() {
        assertEquals("owner", converter.convertToDatabaseColumn(WorkspaceRole.OWNER));
        assertEquals("admin", converter.convertToDatabaseColumn(WorkspaceRole.ADMIN));
    }

    @Test
    void convertToDatabaseColumn_Null() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToEntityAttribute_ValidString() {
        assertEquals(WorkspaceRole.OWNER, converter.convertToEntityAttribute("owner"));
        assertEquals(WorkspaceRole.ADMIN, converter.convertToEntityAttribute("admin"));
        assertEquals(WorkspaceRole.OWNER, converter.convertToEntityAttribute("OWNER"));
    }

    @Test
    void convertToEntityAttribute_Null() {
        assertNull(converter.convertToEntityAttribute(null));
    }
}
