package com.example.how2prompt.modules.template.dto.response;

import com.example.how2prompt.modules.catalog.dto.response.AiModelSummaryResponse;
import com.example.how2prompt.modules.taxonomy.dto.response.CategorySummaryResponse;
import com.example.how2prompt.modules.taxonomy.dto.response.TagResponse;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class TemplateSummaryResponse {
    private UUID id;
    private String slug;
    private Map<String, Object> titleI18n;
    private Map<String, Object> descriptionI18n;
    private String coverImage;
    private long usageCount;
    private int favoriteCount;
    private boolean isOfficial;
    private boolean isPublic;
    private Instant featuredAt;
    private Instant publishedAt;
    private long viewCount;
    private List<CategorySummaryResponse> categories = new ArrayList<>();
    private List<TagResponse> tags = new ArrayList<>();
    private List<AiModelSummaryResponse> models = new ArrayList<>();
}
