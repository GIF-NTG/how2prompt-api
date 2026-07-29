package com.example.how2prompt.modules.template.dto.response;

import com.example.how2prompt.modules.catalog.dto.response.AiModelSummaryResponse;
import com.example.how2prompt.modules.taxonomy.dto.response.CategorySummaryResponse;
import com.example.how2prompt.modules.taxonomy.dto.response.TagResponse;
import com.example.how2prompt.modules.template.dto.TemplateVariableResponse;
import com.example.how2prompt.modules.template.dto.TemplateVariantResponse;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class TemplateDetailResponse {
    private UUID id;
    private UUID workspaceId;
    private String slug;
    private Map<String, Object> titleI18n;
    private Map<String, Object> descriptionI18n;
    private String coverImage;
    private UUID authorId;
    private String authorType;
    private boolean official;
    private boolean isPublic;
    private String status;
    private long usageCount;
    private int favoriteCount;
    private Instant featuredAt;
    private Instant publishedAt;
    private Instant createdAt;
    private Instant updatedAt;
    private long viewCount;

    private TemplateVersionItem currentVersion;

    private List<CategorySummaryResponse> categories = new ArrayList<>();
    private List<TagResponse> tags = new ArrayList<>();
    private List<AiModelSummaryResponse> models = new ArrayList<>();

    @Getter
    @Setter
    public static class TemplateVersionItem {
        private UUID id;
        private int versionNumber;
        private String promptBody;
        private String systemPrompt;
        private String exampleOutput;
        private Map<String, Object> guideI18n;
        private List<TemplateVariableResponse> variables = new ArrayList<>();
        private List<TemplateVariantResponse> variants = new ArrayList<>();
    }
}
