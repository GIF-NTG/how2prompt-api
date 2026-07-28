package com.example.how2prompt.modules.taxonomy.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class UpdateCategoryRequest {

    @Size(max = 60)
    private String slug;

    private Map<String, Object> nameI18n;

    private Map<String, Object> descriptionI18n;

    @Size(max = 60)
    private String icon;

    @Size(max = 20)
    private String color;

    private UUID parentId;

    private Integer sortOrder;

    private Boolean isActive;
}
