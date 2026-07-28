package com.example.how2prompt.modules.taxonomy.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class CategorySummaryResponse {
    private UUID id;
    private String slug;
    private Map<String, Object> nameI18n;
}
