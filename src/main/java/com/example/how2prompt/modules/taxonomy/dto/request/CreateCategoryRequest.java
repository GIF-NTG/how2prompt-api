package com.example.how2prompt.modules.taxonomy.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class CreateCategoryRequest {

    @NotBlank
    @Size(max = 60)
    private String slug;

    @NotNull
    @NotEmpty
    private Map<String, Object> nameI18n;

    private Map<String, Object> descriptionI18n = new HashMap<>();

    @Size(max = 60)
    private String icon;

    @Size(max = 20)
    private String color;

    private UUID parentId;

    private Integer sortOrder = 0;

    private Boolean isActive = true;
}
