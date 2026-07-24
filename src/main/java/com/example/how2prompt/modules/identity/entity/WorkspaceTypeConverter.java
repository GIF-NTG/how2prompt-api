package com.example.how2prompt.modules.identity.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Locale;

/**
 * Enum Java (PERSONAL/TEAM) <-> string lowercase trong DB ('personal'/'team'), khớp
 * CHECK constraint của cột workspaces.type. autoApply=true nên không cần @Convert thủ
 * công ở field.
 */
@Converter(autoApply = true)
public class WorkspaceTypeConverter implements AttributeConverter<WorkspaceType, String> {

    @Override
    public String convertToDatabaseColumn(WorkspaceType attribute) {
        return attribute == null ? null : attribute.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public WorkspaceType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : WorkspaceType.valueOf(dbData.toUpperCase(Locale.ROOT));
    }
}
