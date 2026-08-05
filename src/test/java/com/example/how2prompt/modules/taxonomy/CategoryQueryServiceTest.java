package com.example.how2prompt.modules.taxonomy;

import com.example.how2prompt.common.exception.ResourceNotFoundException;
import com.example.how2prompt.modules.taxonomy.dto.response.CategorySummaryResponse;
import com.example.how2prompt.modules.taxonomy.dto.response.CategoryTreeResponse;
import com.example.how2prompt.modules.taxonomy.entity.Category;
import com.example.how2prompt.modules.taxonomy.repository.CategoryRepository;
import com.example.how2prompt.modules.taxonomy.service.CategoryQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryQueryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryQueryService categoryQueryService;

    private Category parentCategory;
    private Category childCategory;
    private Category invalidChildCategory;

    @BeforeEach
    void setUp() {
        parentCategory = new Category();
        parentCategory.setId(UUID.randomUUID());
        parentCategory.setSlug("parent");

        childCategory = new Category();
        childCategory.setId(UUID.randomUUID());
        childCategory.setSlug("child");
        childCategory.setParent(parentCategory);
        
        Category missingParent = new Category();
        missingParent.setId(UUID.randomUUID());
        
        invalidChildCategory = new Category();
        invalidChildCategory.setId(UUID.randomUUID());
        invalidChildCategory.setSlug("invalid-child");
        invalidChildCategory.setParent(missingParent);
    }

    @Test
    void testFindTree() {
        when(categoryRepository.findAllByIsActiveTrueOrderBySortOrderAsc())
                .thenReturn(List.of(parentCategory, childCategory, invalidChildCategory));
        
        List<CategoryTreeResponse> res = categoryQueryService.findTree();
        assertEquals(1, res.size()); // parent is the only root
        assertEquals(1, res.get(0).getChildren().size());
        assertEquals("child", res.get(0).getChildren().get(0).getSlug());
    }

    @Test
    void testResolveIdBySlug_NullOrBlank() {
        assertNull(categoryQueryService.resolveIdBySlug(null));
        assertNull(categoryQueryService.resolveIdBySlug("  "));
    }

    @Test
    void testResolveIdBySlug_Success() {
        when(categoryRepository.findBySlug("test")).thenReturn(Optional.of(parentCategory));
        assertEquals(parentCategory.getId(), categoryQueryService.resolveIdBySlug("test"));
    }

    @Test
    void testResolveIdBySlug_NotFound() {
        when(categoryRepository.findBySlug("not-found")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> categoryQueryService.resolveIdBySlug("not-found"));
    }

    @Test
    void testFindByIds() {
        when(categoryRepository.findAllById(Set.of(parentCategory.getId())))
                .thenReturn(List.of(parentCategory));
        
        List<CategorySummaryResponse> res = categoryQueryService.findByIds(Set.of(parentCategory.getId()));
        assertEquals(1, res.size());
        assertEquals("parent", res.get(0).getSlug());
    }
}
