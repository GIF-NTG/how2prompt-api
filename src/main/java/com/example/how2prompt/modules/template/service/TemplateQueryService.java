package com.example.how2prompt.modules.template.service;

import com.example.how2prompt.common.exception.BadRequestException;
import com.example.how2prompt.common.exception.ResourceNotFoundException;
import com.example.how2prompt.common.utils.CursorUtil;
import com.example.how2prompt.common.utils.CursorUtil.DecodedCursor;
import com.example.how2prompt.modules.catalog.dto.response.AiModelSummaryResponse;
import com.example.how2prompt.modules.catalog.service.AiModelQueryService;
import com.example.how2prompt.modules.taxonomy.dto.response.CategorySummaryResponse;
import com.example.how2prompt.modules.taxonomy.dto.response.TagResponse;
import com.example.how2prompt.modules.taxonomy.service.CategoryQueryService;
import com.example.how2prompt.modules.taxonomy.service.TagQueryService;
import com.example.how2prompt.modules.template.dto.TemplateVariableResponse;
import com.example.how2prompt.modules.template.dto.TemplateVariantResponse;
import com.example.how2prompt.modules.template.dto.TemplateVersionStatus;
import com.example.how2prompt.modules.template.dto.request.TemplateSearchCriteria;
import com.example.how2prompt.common.response.PageResponse;
import com.example.how2prompt.modules.template.dto.response.TemplateDetailResponse;
import com.example.how2prompt.modules.template.dto.response.TemplateSummaryResponse;
import com.example.how2prompt.modules.template.entity.Template;
import com.example.how2prompt.modules.template.entity.TemplateCategory;
import com.example.how2prompt.modules.template.entity.TemplateModel;
import com.example.how2prompt.modules.template.entity.TemplateTag;
import com.example.how2prompt.modules.template.entity.TemplateVariable;
import com.example.how2prompt.modules.template.entity.TemplateVariant;
import com.example.how2prompt.modules.template.entity.TemplateVersion;
import com.example.how2prompt.modules.template.repository.TemplateCategoryRepository;
import com.example.how2prompt.modules.template.repository.TemplateModelRepository;
import com.example.how2prompt.modules.template.repository.TemplateRepository;
import com.example.how2prompt.modules.template.repository.TemplateTagRepository;
import com.example.how2prompt.modules.template.repository.TemplateVariableRepository;
import com.example.how2prompt.modules.template.repository.TemplateVariantRepository;
import com.example.how2prompt.modules.template.repository.TemplateVersionRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TemplateQueryService {

    private static final String STATUS_PUBLISHED = "published";
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final TemplateRepository templateRepository;
    private final TemplateCategoryRepository templateCategoryRepository;
    private final TemplateTagRepository templateTagRepository;
    private final TemplateModelRepository templateModelRepository;
    private final TemplateVersionRepository templateVersionRepository;
    private final TemplateVariableRepository templateVariableRepository;
    private final TemplateVariantRepository templateVariantRepository;

    private final CategoryQueryService categoryQueryService;
    private final TagQueryService tagQueryService;
    private final AiModelQueryService aiModelQueryService;

    /**
     * Tìm kiếm và lọc template công khai/admin.
     */
    public PageResponse<TemplateSummaryResponse> search(TemplateSearchCriteria criteria, boolean isAdmin) {
        int limit = resolveLimit(criteria);

        // Resolve criteria slugs
        UUID categoryId = categoryQueryService.resolveIdBySlug(criteria.getCategory());
        List<UUID> tagIds = tagQueryService.resolveIdsBySlugs(criteria.getTags());
        UUID modelId = aiModelQueryService.resolveIdByCode(criteria.getModel());

        String status = isAdmin ? null : STATUS_PUBLISHED;
        Boolean isPublic = isAdmin ? null : Boolean.TRUE;

        List<Template> templates;
        if (StringUtils.hasText(criteria.getSearch())) {
            templates = searchWithFts(criteria, status, isPublic, categoryId, tagIds, modelId, limit);
        } else {
            templates = searchWithSpec(criteria, status, isPublic, categoryId, tagIds, modelId, limit);
        }

        // Kiểm tra hasMore & Cắt bớt phần tử thừa
        boolean hasMore = templates.size() > limit;
        List<Template> resultList = hasMore ? templates.subList(0, limit) : templates;

        // Xây dựng nextCursor
        String nextCursor = null;
        if (!resultList.isEmpty() && hasMore) {
            Template lastItem = resultList.get(resultList.size() - 1);
            if ("featured".equals(criteria.getSort())) {
                nextCursor = CursorUtil.encode(lastItem.getFeaturedAt(), lastItem.getId());
            } else if ("trending".equals(criteria.getSort())) {
                nextCursor = CursorUtil.encode(lastItem.getUsageCount(), lastItem.getId());
            } else {
                nextCursor = CursorUtil.encode(lastItem.getCreatedAt(), lastItem.getId());
            }
        }

        // Chuyển sang DTO và batch enrich metadata để chống N+1 queries
        List<TemplateSummaryResponse> dtoList = resultList.stream()
                .map(this::mapToSummaryResponse)
                .collect(Collectors.toList());

        enrichTemplateSummaries(dtoList);

        return new PageResponse<>(dtoList, nextCursor, hasMore);
    }

    /**
     * Chi tiết Template.
     * Kiểm tra quyền truy cập công khai.
     */
    public TemplateDetailResponse getDetail(UUID id, UUID currentUserId, boolean isAdmin) {
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template", id));

        // Kiểm tra quyền xem
        if (!isAdmin) {
            boolean isAuthor = currentUserId != null && currentUserId.equals(template.getAuthorId());
            if (!isAuthor && (!STATUS_PUBLISHED.equals(template.getStatus()) || !template.isPublic())) {
                throw new ResourceNotFoundException("Template", id);
            }
        }

        TemplateDetailResponse response = new TemplateDetailResponse();
        response.setId(template.getId());
        response.setWorkspaceId(template.getWorkspaceId());
        response.setSlug(template.getSlug());
        response.setTitleI18n(template.getTitleI18n());
        response.setDescriptionI18n(template.getDescriptionI18n());
        response.setCoverImage(template.getCoverImage());
        response.setAuthorId(template.getAuthorId());
        response.setAuthorType(template.getAuthorType());
        response.setOfficial(template.isOfficial());
        response.setPublic(template.isPublic());
        response.setStatus(template.getStatus());
        response.setUsageCount(template.getUsageCount());
        response.setFavoriteCount(template.getFavoriteCount());
        response.setFeaturedAt(template.getFeaturedAt());
        response.setPublishedAt(template.getPublishedAt());
        response.setCreatedAt(template.getCreatedAt());
        response.setUpdatedAt(template.getUpdatedAt());

        // Lấy version hiện tại
        if (template.getCurrentVersionId() != null) {
            TemplateVersion version = templateVersionRepository
                    .findByIdAndTemplateId(template.getCurrentVersionId(), template.getId())
                    .or(() -> templateVersionRepository.findByTemplateIdAndCurrentTrue(template.getId()))
                    .orElseThrow(() -> new ResourceNotFoundException("TemplateVersion", template.getCurrentVersionId()));

            TemplateDetailResponse.TemplateVersionItem versionItem = new TemplateDetailResponse.TemplateVersionItem();
            versionItem.setId(version.getId());
            versionItem.setVersionNumber(version.getVersionNumber());
            versionItem.setPromptBody(version.getPromptBody());
            versionItem.setSystemPrompt(version.getSystemPrompt());
            versionItem.setExampleOutput(version.getExampleOutput());
            versionItem.setGuideI18n(version.getGuideI18n());

            // Lấy variables
            List<TemplateVariable> variables = templateVariableRepository.findByTemplateVersionIdOrderBySortOrderAsc(version.getId());
            versionItem.setVariables(variables.stream()
                    .map(v -> new TemplateVariableResponse(
                            v.getId(),
                            v.getTemplateVersion().getId(),
                            v.getVarKey(),
                            v.getLabelI18n(),
                            v.getDescriptionI18n(),
                            v.getPlaceholderI18n(),
                            v.getHelpTextI18n(),
                            v.getInputType(),
                            v.isRequired(),
                            v.getDefaultValue(),
                            v.getOptions(),
                            v.getValidation(),
                            v.getSortOrder()
                    ))
                    .collect(Collectors.toList()));

            // Lấy variants
            List<TemplateVariant> variants = templateVariantRepository.findByTemplateVersionId(version.getId());
            versionItem.setVariants(variants.stream()
                    .map(v -> new TemplateVariantResponse(
                            v.getId(),
                            v.getTemplateVersion().getId(),
                            v.getAiModelId(),
                            v.getPromptBodyOverride(),
                            v.getSystemPromptOverride(),
                            v.getModelConfig(),
                            v.getNotesI18n()
                    ))
                    .collect(Collectors.toList()));

            response.setCurrentVersion(versionItem);
        }

        // Enrich taxonomy & models cho DTO đơn lẻ
        List<TemplateDetailResponse> singleList = new ArrayList<>();
        singleList.add(response);
        enrichTemplateDetails(singleList);

        return response;
    }

    private List<Template> searchWithFts(
            TemplateSearchCriteria criteria,
            String status,
            Boolean isPublic,
            UUID categoryId,
            List<UUID> tagIds,
            UUID modelId,
            int limit
    ) {
        String searchStr = criteria.getSearch().trim();
        DecodedCursor decoded = CursorUtil.decode(criteria.getCursor(), criteria.getSort());
        boolean hasTags = tagIds != null && !tagIds.isEmpty();
        List<UUID> queryTagIds = hasTags ? tagIds : List.of(UUID.randomUUID());

        if ("featured".equals(criteria.getSort())) {
            Instant cursorFeaturedAt = decoded != null ? (Instant) decoded.getSortValue() : null;
            UUID cursorId = decoded != null ? decoded.getId() : null;
            return templateRepository.searchFeatured(searchStr, status, isPublic, categoryId, hasTags, queryTagIds, modelId, cursorFeaturedAt, cursorId, limit + 1);
        } else if ("trending".equals(criteria.getSort())) {
            Long cursorUsageCount = decoded != null ? (Long) decoded.getSortValue() : null;
            UUID cursorId = decoded != null ? decoded.getId() : null;
            return templateRepository.searchTrending(searchStr, status, isPublic, categoryId, hasTags, queryTagIds, modelId, cursorUsageCount, cursorId, limit + 1);
        } else {
            Instant cursorCreatedAt = decoded != null ? (Instant) decoded.getSortValue() : null;
            UUID cursorId = decoded != null ? decoded.getId() : null;
            return templateRepository.searchNewest(searchStr, status, isPublic, categoryId, hasTags, queryTagIds, modelId, cursorCreatedAt, cursorId, limit + 1);
        }
    }

    private List<Template> searchWithSpec(
            TemplateSearchCriteria criteria,
            String status,
            Boolean isPublic,
            UUID categoryId,
            List<UUID> tagIds,
            UUID modelId,
            int limit
    ) {
        DecodedCursor decoded = CursorUtil.decode(criteria.getCursor(), criteria.getSort());

        Specification<Template> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Quyền truy cập
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (isPublic != null) {
                predicates.add(cb.equal(root.get("isPublic"), isPublic));
            }
            if ("featured".equals(criteria.getSort())) {
                predicates.add(cb.isNotNull(root.get("featuredAt")));
            }

            // Bộ lọc category
            if (categoryId != null) {
                Subquery<UUID> subquery = query.subquery(UUID.class);
                Root<TemplateCategory> subRoot = subquery.from(TemplateCategory.class);
                subquery.select(subRoot.get("id").get("templateId"));
                subquery.where(cb.equal(subRoot.get("id").get("categoryId"), categoryId));
                predicates.add(cb.in(root.get("id")).value(subquery));
            }

            // Bộ lọc tags
            if (tagIds != null && !tagIds.isEmpty()) {
                Subquery<UUID> subquery = query.subquery(UUID.class);
                Root<TemplateTag> subRoot = subquery.from(TemplateTag.class);
                subquery.select(subRoot.get("id").get("templateId"));
                subquery.where(subRoot.get("id").get("tagId").in(tagIds));
                predicates.add(cb.in(root.get("id")).value(subquery));
            }

            // Bộ lọc AI Model
            if (modelId != null) {
                Subquery<UUID> subquery = query.subquery(UUID.class);
                Root<TemplateModel> subRoot = subquery.from(TemplateModel.class);
                subquery.select(subRoot.get("id").get("templateId"));
                subquery.where(cb.equal(subRoot.get("id").get("aiModelId"), modelId));
                predicates.add(cb.in(root.get("id")).value(subquery));
            }

            // Xử lý cursor
            if (decoded != null) {
                if ("featured".equals(criteria.getSort())) {
                    Instant cursorFeaturedAt = (Instant) decoded.getSortValue();
                    predicates.add(cb.or(
                            cb.lessThan(root.get("featuredAt"), cursorFeaturedAt),
                            cb.and(
                                    cb.equal(root.get("featuredAt"), cursorFeaturedAt),
                                    cb.lessThan(root.get("id"), decoded.getId())
                            )
                    ));
                } else if ("trending".equals(criteria.getSort())) {
                    Long cursorUsageCount = (Long) decoded.getSortValue();
                    predicates.add(cb.or(
                            cb.lessThan(root.get("usageCount"), cursorUsageCount),
                            cb.and(
                                    cb.equal(root.get("usageCount"), cursorUsageCount),
                                    cb.lessThan(root.get("id"), decoded.getId())
                            )
                    ));
                } else {
                    Instant cursorCreatedAt = (Instant) decoded.getSortValue();
                    predicates.add(cb.or(
                            cb.lessThan(root.get("createdAt"), cursorCreatedAt),
                            cb.and(
                                    cb.equal(root.get("createdAt"), cursorCreatedAt),
                                    cb.lessThan(root.get("id"), decoded.getId())
                            )
                    ));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // Thiết lập Sort & Pageable
        Sort sort;
        if ("featured".equals(criteria.getSort())) {
            sort = Sort.by(Sort.Order.desc("featuredAt").nullsLast(), Sort.Order.desc("id"));
        } else if ("trending".equals(criteria.getSort())) {
            sort = Sort.by(Sort.Order.desc("usageCount"), Sort.Order.desc("id"));
        } else {
            sort = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
        }

        return templateRepository.findAll(spec, PageRequest.of(0, limit + 1, sort)).getContent();
    }

    private static int resolveLimit(TemplateSearchCriteria criteria) {
        if (criteria == null) {
            throw new BadRequestException("Search criteria không được để trống.");
        }

        int limit = criteria.getLimit() != null ? criteria.getLimit() : DEFAULT_LIMIT;
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new BadRequestException(
                    "limit phải nằm trong khoảng 1 đến " + MAX_LIMIT + ".",
                    Map.of("limit", limit, "min", 1, "max", MAX_LIMIT)
            );
        }
        return limit;
    }

    /**
     * Batch enrich metadata cho danh sách summary (tránh N+1).
     */
    private void enrichTemplateSummaries(List<TemplateSummaryResponse> summaries) {
        if (summaries.isEmpty()) return;

        List<UUID> templateIds = summaries.stream().map(TemplateSummaryResponse::getId).toList();

        // 1. Batch load mappings
        List<TemplateCategory> tcList = templateCategoryRepository.findByIdTemplateIdIn(templateIds);
        List<TemplateTag> ttList = templateTagRepository.findByIdTemplateIdIn(templateIds);
        List<TemplateModel> tmList = templateModelRepository.findByIdTemplateIdIn(templateIds);

        // Collect unique entity ids
        Set<UUID> categoryIds = tcList.stream().map(tc -> tc.getId().getCategoryId()).collect(Collectors.toSet());
        Set<UUID> tagIds = ttList.stream().map(tt -> tt.getId().getTagId()).collect(Collectors.toSet());
        Set<UUID> modelIds = tmList.stream().map(tm -> tm.getId().getAiModelId()).collect(Collectors.toSet());

        // 2. Fetch details
        Map<UUID, CategorySummaryResponse> categoryMap = categoryIds.isEmpty() ? Collections.emptyMap() :
                categoryQueryService.findByIds(categoryIds).stream().collect(Collectors.toMap(CategorySummaryResponse::getId, c -> c));

        Map<UUID, TagResponse> tagMap = tagIds.isEmpty() ? Collections.emptyMap() :
                tagQueryService.findByIds(tagIds).stream().collect(Collectors.toMap(TagResponse::getId, t -> t));

        Map<UUID, AiModelSummaryResponse> modelMap = modelIds.isEmpty() ? Collections.emptyMap() :
                aiModelQueryService.findByIds(modelIds).stream().collect(Collectors.toMap(AiModelSummaryResponse::getId, m -> m));

        // Group mappings by template ID
        Map<UUID, List<CategorySummaryResponse>> tcGroup = new HashMap<>();
        Map<UUID, List<TagResponse>> ttGroup = new HashMap<>();
        Map<UUID, List<AiModelSummaryResponse>> tmGroup = new HashMap<>();

        for (TemplateCategory tc : tcList) {
            CategorySummaryResponse summary = categoryMap.get(tc.getId().getCategoryId());
            if (summary != null) {
                tcGroup.computeIfAbsent(tc.getId().getTemplateId(), k -> new ArrayList<>()).add(summary);
            }
        }
        for (TemplateTag tt : ttList) {
            TagResponse summary = tagMap.get(tt.getId().getTagId());
            if (summary != null) {
                ttGroup.computeIfAbsent(tt.getId().getTemplateId(), k -> new ArrayList<>()).add(summary);
            }
        }
        for (TemplateModel tm : tmList) {
            AiModelSummaryResponse summary = modelMap.get(tm.getId().getAiModelId());
            if (summary != null) {
                tmGroup.computeIfAbsent(tm.getId().getTemplateId(), k -> new ArrayList<>()).add(summary);
            }
        }

        // 3. Map to summary DTOs
        for (TemplateSummaryResponse summary : summaries) {
            summary.setCategories(tcGroup.getOrDefault(summary.getId(), Collections.emptyList()));
            summary.setTags(ttGroup.getOrDefault(summary.getId(), Collections.emptyList()));
            summary.setModels(tmGroup.getOrDefault(summary.getId(), Collections.emptyList()));
        }
    }

    /**
     * Batch enrich metadata cho danh sách detail (phục vụ DTO đơn lẻ hoặc batch nếu cần).
     */
    private void enrichTemplateDetails(List<TemplateDetailResponse> details) {
        if (details.isEmpty()) return;

        List<UUID> templateIds = details.stream().map(TemplateDetailResponse::getId).toList();

        List<TemplateCategory> tcList = templateCategoryRepository.findByIdTemplateIdIn(templateIds);
        List<TemplateTag> ttList = templateTagRepository.findByIdTemplateIdIn(templateIds);
        List<TemplateModel> tmList = templateModelRepository.findByIdTemplateIdIn(templateIds);

        Set<UUID> categoryIds = tcList.stream().map(tc -> tc.getId().getCategoryId()).collect(Collectors.toSet());
        Set<UUID> tagIds = ttList.stream().map(tt -> tt.getId().getTagId()).collect(Collectors.toSet());
        Set<UUID> modelIds = tmList.stream().map(tm -> tm.getId().getAiModelId()).collect(Collectors.toSet());

        Map<UUID, CategorySummaryResponse> categoryMap = categoryIds.isEmpty() ? Collections.emptyMap() :
                categoryQueryService.findByIds(categoryIds).stream().collect(Collectors.toMap(CategorySummaryResponse::getId, c -> c));

        Map<UUID, TagResponse> tagMap = tagIds.isEmpty() ? Collections.emptyMap() :
                tagQueryService.findByIds(tagIds).stream().collect(Collectors.toMap(TagResponse::getId, t -> t));

        Map<UUID, AiModelSummaryResponse> modelMap = modelIds.isEmpty() ? Collections.emptyMap() :
                aiModelQueryService.findByIds(modelIds).stream().collect(Collectors.toMap(AiModelSummaryResponse::getId, m -> m));

        Map<UUID, List<CategorySummaryResponse>> tcGroup = new HashMap<>();
        Map<UUID, List<TagResponse>> ttGroup = new HashMap<>();
        Map<UUID, List<AiModelSummaryResponse>> tmGroup = new HashMap<>();

        for (TemplateCategory tc : tcList) {
            CategorySummaryResponse summary = categoryMap.get(tc.getId().getCategoryId());
            if (summary != null) {
                tcGroup.computeIfAbsent(tc.getId().getTemplateId(), k -> new ArrayList<>()).add(summary);
            }
        }
        for (TemplateTag tt : ttList) {
            TagResponse summary = tagMap.get(tt.getId().getTagId());
            if (summary != null) {
                ttGroup.computeIfAbsent(tt.getId().getTemplateId(), k -> new ArrayList<>()).add(summary);
            }
        }
        for (TemplateModel tm : tmList) {
            AiModelSummaryResponse summary = modelMap.get(tm.getId().getAiModelId());
            if (summary != null) {
                tmGroup.computeIfAbsent(tm.getId().getTemplateId(), k -> new ArrayList<>()).add(summary);
            }
        }

        for (TemplateDetailResponse detail : details) {
            detail.setCategories(tcGroup.getOrDefault(detail.getId(), Collections.emptyList()));
            detail.setTags(ttGroup.getOrDefault(detail.getId(), Collections.emptyList()));
            detail.setModels(tmGroup.getOrDefault(detail.getId(), Collections.emptyList()));
        }
    }

    private TemplateSummaryResponse mapToSummaryResponse(Template entity) {
        TemplateSummaryResponse response = new TemplateSummaryResponse();
        response.setId(entity.getId());
        response.setSlug(entity.getSlug());
        response.setTitleI18n(entity.getTitleI18n());
        response.setDescriptionI18n(entity.getDescriptionI18n());
        response.setCoverImage(entity.getCoverImage());
        response.setUsageCount(entity.getUsageCount());
        response.setFavoriteCount(entity.getFavoriteCount());
        response.setOfficial(entity.isOfficial());
        response.setPublic(entity.isPublic());
        response.setFeaturedAt(entity.getFeaturedAt());
        response.setPublishedAt(entity.getPublishedAt());
        return response;
    }

    /**
     * Kiểm tra trạng thái của template và so sánh version đang dùng với current version.
     * Dùng cho Prompt History Detail (US-4.3).
     */
    public TemplateVersionStatus checkTemplateVersionStatus(UUID templateId, UUID templateVersionId) {
        if (templateId == null) {
            return new TemplateVersionStatus(true, false, null);
        }

        java.util.Optional<Template> templateOpt = templateRepository.findById(templateId);
        if (templateOpt.isEmpty()) {
            return new TemplateVersionStatus(true, false, null);
        }

        Template template = templateOpt.get();
        UUID currentVersionId = template.getCurrentVersionId();
        if (currentVersionId == null) {
            return new TemplateVersionStatus(false, false, null);
        }

        if (currentVersionId.equals(templateVersionId)) {
            return new TemplateVersionStatus(false, false, null);
        }

        // Fetch version numbers to compare
        java.util.Optional<TemplateVersion> historyVersionOpt = templateVersionRepository.findById(templateVersionId);
        java.util.Optional<TemplateVersion> currentVersionOpt = templateVersionRepository.findById(currentVersionId);

        if (historyVersionOpt.isPresent() && currentVersionOpt.isPresent()) {
            int historyVerNum = historyVersionOpt.get().getVersionNumber();
            int currentVerNum = currentVersionOpt.get().getVersionNumber();
            if (currentVerNum > historyVerNum) {
                return new TemplateVersionStatus(false, true, "v" + currentVerNum);
            }
        }

        return new TemplateVersionStatus(false, false, null);
    }
}
