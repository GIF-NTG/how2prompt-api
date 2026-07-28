package com.example.how2prompt.modules.taxonomy.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class TagResponse {
    private UUID id;
    private String slug;
    private String name;
    private Integer usageCount;
    private Instant createdAt;
}
