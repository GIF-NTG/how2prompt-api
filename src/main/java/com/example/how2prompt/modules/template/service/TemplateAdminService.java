package com.example.how2prompt.modules.template.service;

import com.example.how2prompt.common.exception.BadRequestException;
import com.example.how2prompt.common.exception.ConflictException;
import com.example.how2prompt.common.exception.ResourceNotFoundException;
import com.example.how2prompt.common.security.AuthenticatedUser;
import com.example.how2prompt.modules.catalog.service.AiModelQueryService;
import com.example.how2prompt.modules.template.dto.CreateTemplateRequest;
import com.example.how2prompt.modules.template.dto.CreateVariableRequest;
import com.example.how2prompt.modules.template.dto.CreateVariantRequest;
import com.example.how2prompt.modules.template.dto.TemplateResponse;
import com.example.how2prompt.modules.template.dto.TemplateVariableResponse;
import com.example.how2prompt.modules.template.dto.TemplateVariantResponse;
import com.example.how2prompt.modules.template.dto.UpdateTemplateRequest;
import com.example.how2prompt.modules.template.entity.Template;
import com.example.how2prompt.modules.template.entity.TemplateCategory;
import com.example.how2prompt.modules.template.entity.TemplateCategoryId;
import com.example.how2prompt.modules.template.entity.TemplateModel;
import com.example.how2prompt.modules.template.entity.TemplateModelId;
import com.example.how2prompt.modules.template.entity.TemplateTag;
import com.example.how2prompt.modules.template.entity.TemplateTagId;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TemplateAdminService {

    private static final String STATUS_DRAFT = "draft";
    private static final String STATUS_PUBLISHED = "published";
    private static final String AUTHOR_TYPE_ADMIN = "admin";

    private final TemplateRepository templateRepository;
    private final TemplateVersionRepository templateVersionRepository;
    private final TemplateVariableRepository templateVariableRepository;
    private final TemplateVariantRepository templateVariantRepository;
    private final TemplateCategoryRepository templateCategoryRepository;
    private final TemplateTagRepository templateTagRepository;
    private final TemplateModelRepository templateModelRepository;
    private final AiModelQueryService aiModelQueryService;

    /**
     * Tạo template draft + version 1 (is_current=true) trong một transaction.
     * {@code workspace_id}/{@code author_id} lấy từ {@link AuthenticatedUser}, không từ request.
     */
    @Transactional
    public TemplateResponse createTemplate(CreateTemplateRequest request, AuthenticatedUser currentUser) {
        requireWorkspace(currentUser);

        if (templateRepository.existsByWorkspaceIdAndSlug(currentUser.workspaceId(), request.getSlug())) {
            throw ConflictException.alreadyExists("Template", "slug", request.getSlug());
        }

        Template template = new Template();
        template.setWorkspaceId(currentUser.workspaceId());
        template.setSlug(request.getSlug().trim());
        template.setTitleI18n(copyMap(request.getTitleI18n()));
        template.setDescriptionI18n(copyMap(request.getDescriptionI18n()));
        template.setCoverImage(blankToNull(request.getCoverImage()));
        template.setAuthorId(currentUser.userId());
        template.setAuthorType(AUTHOR_TYPE_ADMIN);
        template.setOfficial(false);
        template.setPublic(false);
        template.setStatus(STATUS_DRAFT);
        template = templateRepository.save(template);

        TemplateVersion version = createCurrentVersion(
                template,
                1,
                request.getPromptBody(),
                request.getSystemPrompt(),
                request.getExampleOutput(),
                copyMap(request.getGuideI18n()),
                currentUser.userId()
        );

        template.setCurrentVersionId(version.getId());
        template = templateRepository.save(template);

        return toTemplateResponse(template);
    }

    /**
     * Cập nhật metadata, taxonomy join, publish/unpublish.
     */
    @Transactional
    public TemplateResponse updateTemplate(UUID templateId, UpdateTemplateRequest request) {
        Template template = getTemplateOrThrow(templateId);

        if (request.getTitleI18n() != null) {
            if (request.getTitleI18n().isEmpty()) {
                throw new BadRequestException("titleI18n không được rỗng.");
            }
            template.setTitleI18n(copyMap(request.getTitleI18n()));
        }
        if (request.getDescriptionI18n() != null) {
            template.setDescriptionI18n(copyMap(request.getDescriptionI18n()));
        }
        if (request.getCoverImage() != null) {
            template.setCoverImage(blankToNull(request.getCoverImage()));
        }
        if (request.getIsPublic() != null) {
            template.setPublic(request.getIsPublic());
        }
        if (request.getOfficial() != null) {
            template.setOfficial(request.getOfficial());
        }
        if (request.getStatus() != null) {
            applyStatus(template, request.getStatus());
        }

        if (request.getCategoryIds() != null) {
            replaceCategories(template, request.getCategoryIds());
        }
        if (request.getTagIds() != null) {
            replaceTags(template, request.getTagIds());
        }
        if (request.getModelIds() != null) {
            replaceModels(template, request.getModelIds(), request.getPrimaryModelId());
        } else if (request.getPrimaryModelId() != null) {
            updatePrimaryModel(template.getId(), request.getPrimaryModelId());
        }

        template = templateRepository.save(template);
        return toTemplateResponse(template);
    }

    /**
     * Thêm variable vào current version của template.
     */
    @Transactional
    public TemplateVariableResponse addVariable(UUID templateId, CreateVariableRequest request) {
        Template template = getTemplateOrThrow(templateId);
        TemplateVersion currentVersion = getCurrentVersionOrThrow(template);

        String varKey = request.getVarKey().trim();
        if (templateVariableRepository.findByTemplateVersionIdAndVarKey(currentVersion.getId(), varKey).isPresent()) {
            throw ConflictException.alreadyExists("TemplateVariable", "varKey", varKey);
        }

        TemplateVariable variable = new TemplateVariable();
        variable.setTemplateVersion(currentVersion);
        variable.setVarKey(varKey);
        variable.setLabelI18n(copyMap(request.getLabelI18n()));
        variable.setDescriptionI18n(copyMap(request.getDescriptionI18n()));
        variable.setPlaceholderI18n(copyMap(request.getPlaceholderI18n()));
        variable.setHelpTextI18n(copyMap(request.getHelpTextI18n()));
        variable.setInputType(request.getInputType().trim());
        variable.setRequired(request.isRequired());
        variable.setDefaultValue(request.getDefaultValue());
        variable.setOptions(request.getOptions() != null ? new ArrayList<>(request.getOptions()) : new ArrayList<>());
        variable.setValidation(copyMap(request.getValidation()));
        variable.setSortOrder(request.getSortOrder());

        variable = templateVariableRepository.save(variable);
        return toVariableResponse(variable);
    }

    /**
     * Thêm variant theo AI model vào current version.
     * Validate {@code aiModelId} qua {@link AiModelQueryService} — không import entity catalog.
     */
    @Transactional
    public TemplateVariantResponse addVariant(UUID templateId, CreateVariantRequest request) {
        Template template = getTemplateOrThrow(templateId);
        TemplateVersion currentVersion = getCurrentVersionOrThrow(template);

        aiModelQueryService.getByIdOrThrow(request.getAiModelId());

        if (templateVariantRepository
                .findByTemplateVersionIdAndAiModelId(currentVersion.getId(), request.getAiModelId())
                .isPresent()) {
            throw ConflictException.alreadyExists("TemplateVariant", "aiModelId", request.getAiModelId());
        }

        TemplateVariant variant = new TemplateVariant();
        variant.setTemplateVersion(currentVersion);
        variant.setAiModelId(request.getAiModelId());
        variant.setPromptBodyOverride(request.getPromptBodyOverride());
        variant.setSystemPromptOverride(request.getSystemPromptOverride());
        variant.setModelConfig(copyMap(request.getModelConfig()));
        variant.setNotesI18n(copyMap(request.getNotesI18n()));

        variant = templateVariantRepository.save(variant);
        return toVariantResponse(variant);
    }

    /**
     * Soft delete a template by setting its deleted_at timestamp.
     */
    @Transactional
    public void deleteTemplate(UUID templateId) {
        Template template = getTemplateOrThrow(templateId);
        template.setDeletedAt(Instant.now());
        templateRepository.save(template);
    }

    // -------------------------------------------------------------------------
    // Versioning helpers — luôn clear is_current cũ trước khi set true (partial unique index)
    // -------------------------------------------------------------------------

    /**
     * Tạo version mới và đánh dấu current. Phải gọi trong {@code @Transactional}.
     * Set version cũ {@code is_current=false} + flush trước khi insert version mới
     * để thỏa {@code idx_template_versions_one_current}.
     */
    private TemplateVersion createCurrentVersion(
            Template template,
            int versionNumber,
            String promptBody,
            String systemPrompt,
            String exampleOutput,
            Map<String, Object> guideI18n,
            UUID createdBy
    ) {
        clearCurrentVersion(template.getId());

        TemplateVersion version = new TemplateVersion();
        version.setTemplate(template);
        version.setVersionNumber(versionNumber);
        version.setPromptBody(promptBody);
        version.setSystemPrompt(systemPrompt);
        version.setExampleOutput(exampleOutput);
        version.setGuideI18n(guideI18n != null ? guideI18n : new HashMap<>());
        version.setCurrent(true);
        version.setCreatedBy(createdBy);
        return templateVersionRepository.save(version);
    }

    private void clearCurrentVersion(UUID templateId) {
        templateVersionRepository.findByTemplateIdAndCurrentTrue(templateId).ifPresent(existing -> {
            existing.setCurrent(false);
            templateVersionRepository.saveAndFlush(existing);
        });
    }

    // -------------------------------------------------------------------------
    // Join table replace
    // -------------------------------------------------------------------------

    private void replaceCategories(Template template, List<UUID> categoryIds) {
        templateCategoryRepository.deleteByIdTemplateId(template.getId());
        templateCategoryRepository.flush();
        for (UUID categoryId : distinct(categoryIds)) {
            TemplateCategory row = new TemplateCategory();
            row.setId(new TemplateCategoryId(template.getId(), categoryId));
            row.setTemplate(template);
            templateCategoryRepository.save(row);
        }
    }

    private void replaceTags(Template template, List<UUID> tagIds) {
        templateTagRepository.deleteByIdTemplateId(template.getId());
        templateTagRepository.flush();
        for (UUID tagId : distinct(tagIds)) {
            TemplateTag row = new TemplateTag();
            row.setId(new TemplateTagId(template.getId(), tagId));
            row.setTemplate(template);
            templateTagRepository.save(row);
        }
    }

    private void replaceModels(Template template, List<UUID> modelIds, UUID primaryModelId) {
        List<UUID> distinctIds = distinct(modelIds);
        for (UUID modelId : distinctIds) {
            aiModelQueryService.getByIdOrThrow(modelId);
        }
        if (primaryModelId != null && !distinctIds.contains(primaryModelId)) {
            throw new BadRequestException("primaryModelId phải nằm trong modelIds.");
        }

        templateModelRepository.deleteByIdTemplateId(template.getId());
        templateModelRepository.flush();

        UUID resolvedPrimary = primaryModelId != null
                ? primaryModelId
                : (distinctIds.isEmpty() ? null : distinctIds.getFirst());

        for (UUID modelId : distinctIds) {
            TemplateModel row = new TemplateModel();
            row.setId(new TemplateModelId(template.getId(), modelId));
            row.setTemplate(template);
            row.setPrimary(modelId.equals(resolvedPrimary));
            templateModelRepository.save(row);
        }
    }

    private void updatePrimaryModel(UUID templateId, UUID primaryModelId) {
        List<TemplateModel> models = templateModelRepository.findByIdTemplateId(templateId);
        if (models.isEmpty()) {
            throw new BadRequestException("Template chưa gắn AI model nào.");
        }
        boolean found = false;
        for (TemplateModel model : models) {
            boolean isPrimary = primaryModelId.equals(model.getId().getAiModelId());
            model.setPrimary(isPrimary);
            if (isPrimary) {
                found = true;
            }
        }
        if (!found) {
            throw new BadRequestException("primaryModelId không thuộc các model đã gán cho template.");
        }
        templateModelRepository.saveAll(models);
    }

    private void validateTemplatePlaceholders(Template template) {
        TemplateVersion version = getCurrentVersionOrThrow(template);
        List<TemplateVariable> variables = templateVariableRepository.findByTemplateVersionIdOrderBySortOrderAsc(version.getId());
        java.util.Set<String> definedKeys = variables.stream()
                .map(TemplateVariable::getVarKey)
                .collect(java.util.stream.Collectors.toSet());

        java.util.Set<String> placeholders = new java.util.HashSet<>();
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{\\{([a-zA-Z0-9_]+)\\}\\}");

        if (version.getPromptBody() != null) {
            java.util.regex.Matcher matcher = pattern.matcher(version.getPromptBody());
            while (matcher.find()) {
                placeholders.add(matcher.group(1));
            }
        }
        if (version.getSystemPrompt() != null) {
            java.util.regex.Matcher matcher = pattern.matcher(version.getSystemPrompt());
            while (matcher.find()) {
                placeholders.add(matcher.group(1));
            }
        }

        placeholders.remove("__extra__");

        List<String> missingKeys = placeholders.stream()
                .filter(key -> !definedKeys.contains(key))
                .toList();

        if (!missingKeys.isEmpty()) {
            throw new BadRequestException(
                    "Không thể xuất bản template do thiếu định nghĩa các biến dynamic form: " + String.join(", ", missingKeys),
                    Map.of("missingVariables", missingKeys)
            );
        }
    }

    private void applyStatus(Template template, String rawStatus) {
        String status = rawStatus.trim().toLowerCase(Locale.ROOT);
        switch (status) {
            case STATUS_PUBLISHED -> {
                validateTemplatePlaceholders(template);
                template.setStatus(STATUS_PUBLISHED);
                if (template.getPublishedAt() == null) {
                    template.setPublishedAt(Instant.now());
                }
            }
            case STATUS_DRAFT -> {
                template.setStatus(STATUS_DRAFT);
                template.setPublishedAt(null);
            }
            default -> throw new BadRequestException(
                    "status không hợp lệ. Chỉ chấp nhận: draft, published.",
                    Map.of("status", rawStatus)
            );
        }
    }

    // -------------------------------------------------------------------------
    // Load helpers
    // -------------------------------------------------------------------------

    private Template getTemplateOrThrow(UUID templateId) {
        return templateRepository.findById(templateId)
                .orElseThrow(() -> ResourceNotFoundException.of("Template", templateId));
    }

    private TemplateVersion getCurrentVersionOrThrow(Template template) {
        if (template.getCurrentVersionId() == null) {
            throw new BadRequestException("Template chưa có current version.");
        }
        return templateVersionRepository.findByIdAndTemplateId(
                        template.getCurrentVersionId(),
                        template.getId()
                )
                .or(() -> templateVersionRepository.findByTemplateIdAndCurrentTrue(template.getId()))
                .orElseThrow(() -> new BadRequestException("Không tìm thấy current version của template."));
    }

    private static void requireWorkspace(AuthenticatedUser currentUser) {
        if (currentUser == null || currentUser.workspaceId() == null) {
            throw new BadRequestException("Authenticated user thiếu workspace_id.");
        }
        if (currentUser.userId() == null) {
            throw new BadRequestException("Authenticated user thiếu user_id.");
        }
    }

    // -------------------------------------------------------------------------
    // Mapping
    // -------------------------------------------------------------------------

    private TemplateResponse toTemplateResponse(Template template) {
        List<UUID> categoryIds = templateCategoryRepository.findByIdTemplateId(template.getId()).stream()
                .map(row -> row.getId().getCategoryId())
                .toList();
        List<UUID> tagIds = templateTagRepository.findByIdTemplateId(template.getId()).stream()
                .map(row -> row.getId().getTagId())
                .toList();
        List<TemplateResponse.TemplateModelItem> models = templateModelRepository.findByIdTemplateId(template.getId())
                .stream()
                .map(row -> new TemplateResponse.TemplateModelItem(row.getId().getAiModelId(), row.isPrimary()))
                .toList();

        return new TemplateResponse(
                template.getId(),
                template.getWorkspaceId(),
                template.getSlug(),
                template.getTitleI18n(),
                template.getDescriptionI18n(),
                template.getCoverImage(),
                template.getAuthorId(),
                template.getAuthorType(),
                template.isOfficial(),
                template.isPublic(),
                template.getStatus(),
                template.getCurrentVersionId(),
                template.getUsageCount(),
                template.getFavoriteCount(),
                template.getFeaturedAt(),
                template.getPublishedAt(),
                template.getCreatedAt(),
                template.getUpdatedAt(),
                categoryIds,
                tagIds,
                models
        );
    }

    private static TemplateVariableResponse toVariableResponse(TemplateVariable variable) {
        return new TemplateVariableResponse(
                variable.getId(),
                variable.getTemplateVersion().getId(),
                variable.getVarKey(),
                variable.getLabelI18n(),
                variable.getDescriptionI18n(),
                variable.getPlaceholderI18n(),
                variable.getHelpTextI18n(),
                variable.getInputType(),
                variable.isRequired(),
                variable.getDefaultValue(),
                variable.getOptions(),
                variable.getValidation(),
                variable.getSortOrder()
        );
    }

    private static TemplateVariantResponse toVariantResponse(TemplateVariant variant) {
        return new TemplateVariantResponse(
                variant.getId(),
                variant.getTemplateVersion().getId(),
                variant.getAiModelId(),
                variant.getPromptBodyOverride(),
                variant.getSystemPromptOverride(),
                variant.getModelConfig(),
                variant.getNotesI18n()
        );
    }

    private static Map<String, Object> copyMap(Map<String, Object> source) {
        return source == null ? new HashMap<>() : new HashMap<>(source);
    }

    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private static List<UUID> distinct(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ids.stream().distinct().toList();
    }
}
