package com.example.how2prompt.modules.identity.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Locale;

@Converter(autoApply = true)
public class WorkspaceRoleConverter implements AttributeConverter<WorkspaceRole, String> {

    @Override
    public String convertToDatabaseColumn(WorkspaceRole attribute) {
        return attribute == null ? null : attribute.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public WorkspaceRole convertToEntityAttribute(String dbData) {
        return dbData == null ? null : WorkspaceRole.valueOf(dbData.toUpperCase(Locale.ROOT));
    }
}
