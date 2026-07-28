package com.example.how2prompt.modules.taxonomy.service;

import com.example.how2prompt.common.exception.BadRequestException;
import com.example.how2prompt.common.exception.ConflictException;
import com.example.how2prompt.common.exception.ResourceNotFoundException;
import com.example.how2prompt.modules.taxonomy.dto.request.CreateCategoryRequest;
import com.example.how2prompt.modules.taxonomy.dto.request.CreateTagRequest;
import com.example.how2prompt.modules.taxonomy.dto.request.UpdateCategoryRequest;
import com.example.how2prompt.modules.taxonomy.dto.response.CategoryTreeResponse;
import com.example.how2prompt.modules.taxonomy.dto.response.TagResponse;
import com.example.how2prompt.modules.taxonomy.entity.Category;
import com.example.how2prompt.modules.taxonomy.entity.Tag;
import com.example.how2prompt.modules.taxonomy.repository.CategoryRepository;
import com.example.how2prompt.modules.taxonomy.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TaxonomyAdminService {

    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final CategoryQueryService categoryQueryService;
    private final TagQueryService tagQueryService;

    /**
     * Tạo Category mới.
     * Kiểm tra trùng slug và tính tuần hoàn của parent_id.
     */
    public CategoryTreeResponse createCategory(CreateCategoryRequest request) {
        String slug = request.getSlug().trim();
        if (categoryRepository.existsBySlug(slug)) {
            throw ConflictException.alreadyExists("Category", "slug", slug);
        }

        Category category = new Category();
        category.setSlug(slug);
        category.setNameI18n(request.getNameI18n());
        category.setDescriptionI18n(request.getDescriptionI18n());
        category.setIcon(request.getIcon());
        category.setColor(request.getColor());
        category.setSortOrder(request.getSortOrder());
        category.setIsActive(request.getIsActive());

        if (request.getParentId() != null) {
            validateCategoryParent(null, request.getParentId());
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", request.getParentId()));
            category.setParent(parent);
        }

        category = categoryRepository.save(category);
        return categoryQueryService.mapToTreeResponse(category);
    }

    /**
     * Cập nhật Category.
     * Kiểm tra trùng slug, đổi parent_id và ngăn chặn lặp vòng (cycle detection).
     */
    public CategoryTreeResponse updateCategory(UUID id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        if (request.getSlug() != null) {
            String newSlug = request.getSlug().trim();
            if (!newSlug.equals(category.getSlug()) && categoryRepository.existsBySlug(newSlug)) {
                throw ConflictException.alreadyExists("Category", "slug", newSlug);
            }
            category.setSlug(newSlug);
        }

        if (request.getNameI18n() != null) {
            category.setNameI18n(request.getNameI18n());
        }
        if (request.getDescriptionI18n() != null) {
            category.setDescriptionI18n(request.getDescriptionI18n());
        }
        if (request.getIcon() != null) {
            category.setIcon(request.getIcon());
        }
        if (request.getColor() != null) {
            category.setColor(request.getColor());
        }
        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }
        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }

        if (request.getParentId() != null || (request.getParentId() == null && category.getParent() != null)) {
            UUID newParentId = request.getParentId();
            if (newParentId != null) {
                validateCategoryParent(category.getId(), newParentId);
                Category parent = categoryRepository.findById(newParentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Category", newParentId));
                category.setParent(parent);
            } else {
                category.setParent(null);
            }
        }

        category = categoryRepository.save(category);
        return categoryQueryService.mapToTreeResponse(category);
    }

    /**
     * Vô hiệu hóa Category (Soft delete).
     */
    public void deleteCategory(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        category.setIsActive(false);
        categoryRepository.save(category);
    }

    /**
     * Tạo Tag mới.
     */
    public TagResponse createTag(CreateTagRequest request) {
        String slug = request.getSlug().trim();
        if (tagRepository.existsBySlug(slug)) {
            throw ConflictException.alreadyExists("Tag", "slug", slug);
        }

        Tag tag = new Tag();
        tag.setSlug(slug);
        tag.setName(request.getName().trim());
        tag.setUsageCount(0);

        tag = tagRepository.save(tag);
        return tagQueryService.mapToTagResponse(tag);
    }

    /**
     * Cập nhật Tag.
     */
    public TagResponse updateTag(UUID id, CreateTagRequest request) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", id));

        String newSlug = request.getSlug().trim();
        if (!newSlug.equals(tag.getSlug()) && tagRepository.existsBySlug(newSlug)) {
            throw ConflictException.alreadyExists("Tag", "slug", newSlug);
        }

        tag.setSlug(newSlug);
        tag.setName(request.getName().trim());
        tag = tagRepository.save(tag);
        return tagQueryService.mapToTagResponse(tag);
    }

    /**
     * Xóa cứng Tag.
     */
    public void deleteTag(UUID id) {
        if (!tagRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tag", id);
        }
        tagRepository.deleteById(id);
    }

    /**
     * Ngăn chặn lặp vòng trong quan hệ cha-con.
     * Duyệt ngược từ newParentId lên gốc, nếu gặp trùng lặp thì báo lỗi.
     */
    private void validateCategoryParent(UUID categoryId, UUID newParentId) {
        if (categoryId != null && categoryId.equals(newParentId)) {
            throw new BadRequestException("Category cannot be its own parent");
        }

        Set<UUID> visited = new HashSet<>();
        if (categoryId != null) {
            visited.add(categoryId);
        }

        UUID nextParentId = newParentId;
        while (nextParentId != null) {
            if (!visited.add(nextParentId)) {
                throw new BadRequestException("Cyclic parent hierarchy detected");
            }
            final UUID currentParentId = nextParentId;
            Category parent = categoryRepository.findById(currentParentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category", currentParentId));
            nextParentId = parent.getParent() != null ? parent.getParent().getId() : null;
        }
    }
}
