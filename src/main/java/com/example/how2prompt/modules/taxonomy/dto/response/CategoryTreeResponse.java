package com.example.how2prompt.modules.taxonomy.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class CategoryTreeResponse {
    private UUID id;
    private String slug;
    private Map<String, Object> nameI18n;
    private Map<String, Object> descriptionI18n;
    private String icon;
    private String color;
    private UUID parentId;
    private Integer sortOrder;
    private Boolean isActive;
    private List<CategoryTreeResponse> children = new ArrayList<>();
}
