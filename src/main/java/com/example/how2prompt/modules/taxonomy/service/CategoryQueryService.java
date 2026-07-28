package com.example.how2prompt.modules.taxonomy.service;

import com.example.how2prompt.common.exception.ResourceNotFoundException;
import com.example.how2prompt.modules.taxonomy.dto.response.CategorySummaryResponse;
import com.example.how2prompt.modules.taxonomy.dto.response.CategoryTreeResponse;
import com.example.how2prompt.modules.taxonomy.entity.Category;
import com.example.how2prompt.modules.taxonomy.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryQueryService {

    private final CategoryRepository categoryRepository;

    /**
     * Lấy toàn bộ cây danh mục đang hoạt động (isActive = true).
     * Dựng cây trên bộ nhớ bằng LinkedHashMap để tránh đệ quy lazy-load từ DB.
     */
    public List<CategoryTreeResponse> findTree() {
        List<Category> allCategories = categoryRepository.findAllByIsActiveTrueOrderBySortOrderAsc();

        // Chuyển đổi toàn bộ sang DTO và gom vào Map để tra cứu nhanh
        Map<UUID, CategoryTreeResponse> dtoMap = new LinkedHashMap<>();
        for (Category category : allCategories) {
            dtoMap.put(category.getId(), mapToTreeResponse(category));
        }

        // Dựng cây liên kết
        List<CategoryTreeResponse> roots = new ArrayList<>();
        for (Category category : allCategories) {
            CategoryTreeResponse currentDto = dtoMap.get(category.getId());
            if (category.getParent() == null) {
                roots.add(currentDto);
            } else {
                CategoryTreeResponse parentDto = dtoMap.get(category.getParent().getId());
                if (parentDto != null) {
                    parentDto.getChildren().add(currentDto);
                }
            }
        }

        return roots;
    }

    /**
     * Resolve slug thành UUID.
     * Ném ResourceNotFoundException nếu không tìm thấy.
     */
    public UUID resolveIdBySlug(String slug) {
        if (slug == null || slug.isBlank()) {
            return null;
        }
        return categoryRepository.findBySlug(slug)
                .map(Category::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", slug));
    }

    /**
     * Lấy danh sách tóm tắt danh mục theo tập hợp IDs (phục vụ batch enrichment).
     */
    public List<CategorySummaryResponse> findByIds(Set<UUID> ids) {
        return categoryRepository.findAllById(ids).stream()
                .map(this::mapToSummaryResponse)
                .collect(Collectors.toList());
    }

    public CategoryTreeResponse mapToTreeResponse(Category entity) {
        CategoryTreeResponse response = new CategoryTreeResponse();
        response.setId(entity.getId());
        response.setSlug(entity.getSlug());
        response.setNameI18n(entity.getNameI18n());
        response.setDescriptionI18n(entity.getDescriptionI18n());
        response.setIcon(entity.getIcon());
        response.setColor(entity.getColor());
        response.setParentId(entity.getParent() != null ? entity.getParent().getId() : null);
        response.setSortOrder(entity.getSortOrder());
        response.setIsActive(entity.getIsActive());
        return response;
    }

    private CategorySummaryResponse mapToSummaryResponse(Category entity) {
        CategorySummaryResponse response = new CategorySummaryResponse();
        response.setId(entity.getId());
        response.setSlug(entity.getSlug());
        response.setNameI18n(entity.getNameI18n());
        return response;
    }
}
