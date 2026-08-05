package com.example.how2prompt.modules.taxonomy;

import com.example.how2prompt.common.exception.BadRequestException;
import com.example.how2prompt.common.exception.ConflictException;
import com.example.how2prompt.common.exception.ResourceNotFoundException;
import com.example.how2prompt.modules.taxonomy.dto.request.CreateCategoryRequest;
import com.example.how2prompt.modules.taxonomy.dto.request.CreateTagRequest;
import com.example.how2prompt.modules.taxonomy.dto.request.TagMergeRequest;
import com.example.how2prompt.modules.taxonomy.dto.request.UpdateCategoryRequest;
import com.example.how2prompt.modules.taxonomy.dto.response.CategoryTreeResponse;
import com.example.how2prompt.modules.taxonomy.entity.Category;
import com.example.how2prompt.modules.taxonomy.entity.Tag;
import com.example.how2prompt.modules.taxonomy.repository.CategoryRepository;
import com.example.how2prompt.modules.taxonomy.repository.TagRepository;
import com.example.how2prompt.modules.taxonomy.service.CategoryQueryService;
import com.example.how2prompt.modules.taxonomy.service.TagQueryService;
import com.example.how2prompt.modules.taxonomy.service.TaxonomyAdminService;
import com.example.how2prompt.modules.template.entity.Template;
import com.example.how2prompt.modules.template.entity.TemplateTag;
import com.example.how2prompt.modules.template.entity.TemplateTagId;
import com.example.how2prompt.modules.template.repository.TemplateTagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaxonomyAdminServiceTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private TagRepository tagRepository;
    @Mock private CategoryQueryService categoryQueryService;
    @Mock private TagQueryService tagQueryService;
    @Mock private TemplateTagRepository templateTagRepository;

    @InjectMocks
    private TaxonomyAdminService taxonomyAdminService;

    private Category category;
    private Tag tag;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(UUID.randomUUID());
        category.setSlug("test-cat");

        tag = new Tag();
        tag.setId(UUID.randomUUID());
        tag.setSlug("test-tag");
        tag.setUsageCount(10);
    }

    @Test
    void testCreateCategory_Success() {
        CreateCategoryRequest req = new CreateCategoryRequest();
        req.setSlug("new-cat");
        req.setNameI18n(Map.of("en", "new name"));
        req.setDescriptionI18n(Map.of("en", "new desc"));
        req.setIcon("icon");
        req.setColor("color");
        req.setSortOrder(1);
        req.setIsActive(true);
        req.setParentId(category.getId());

        when(categoryRepository.existsBySlug("new-cat")).thenReturn(false);
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));
        when(categoryQueryService.mapToTreeResponse(any(Category.class))).thenReturn(new CategoryTreeResponse());

        assertNotNull(taxonomyAdminService.createCategory(req));
    }

    @Test
    void testCreateCategory_Conflict() {
        CreateCategoryRequest req = new CreateCategoryRequest();
        req.setSlug("conflict");
        when(categoryRepository.existsBySlug("conflict")).thenReturn(true);
        assertThrows(ConflictException.class, () -> taxonomyAdminService.createCategory(req));
    }

    @Test
    void testUpdateCategory_SuccessAllFields() {
        UpdateCategoryRequest req = new UpdateCategoryRequest();
        req.setSlug("updated-cat");
        req.setNameI18n(Map.of("en", "updated name"));
        req.setDescriptionI18n(Map.of("en", "updated desc"));
        req.setIcon("updated icon");
        req.setColor("updated color");
        req.setSortOrder(2);
        req.setIsActive(false);

        Category parent = new Category();
        parent.setId(UUID.randomUUID());
        req.setParentId(parent.getId());

        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(categoryRepository.existsBySlug("updated-cat")).thenReturn(false);
        when(categoryRepository.findById(parent.getId())).thenReturn(Optional.of(parent));
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));

        taxonomyAdminService.updateCategory(category.getId(), req);
        assertEquals("updated-cat", category.getSlug());
        assertEquals(parent, category.getParent());
    }

    @Test
    void testUpdateCategory_NullParent_RemovesParent() {
        category.setParent(new Category());
        UpdateCategoryRequest req = new UpdateCategoryRequest();
        req.setParentId(null);
        
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));
        
        taxonomyAdminService.updateCategory(category.getId(), req);
        assertNull(category.getParent());
    }

    @Test
    void testUpdateCategory_CyclicParent_SameAsSelf() {
        UpdateCategoryRequest req = new UpdateCategoryRequest();
        req.setParentId(category.getId());
        
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        assertThrows(BadRequestException.class, () -> taxonomyAdminService.updateCategory(category.getId(), req));
    }

    @Test
    void testUpdateCategory_CyclicParent_Loop() {
        Category parent = new Category();
        parent.setId(UUID.randomUUID());
        
        Category grandParent = new Category();
        grandParent.setId(UUID.randomUUID());
        parent.setParent(grandParent);
        
        grandParent.setParent(category); // Loop: category -> parent -> grandParent -> category

        UpdateCategoryRequest req = new UpdateCategoryRequest();
        req.setParentId(parent.getId());
        
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(categoryRepository.findById(parent.getId())).thenReturn(Optional.of(parent));
        when(categoryRepository.findById(grandParent.getId())).thenReturn(Optional.of(grandParent));
        
        assertThrows(BadRequestException.class, () -> taxonomyAdminService.updateCategory(category.getId(), req));
    }

    @Test
    void testDeleteCategory() {
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        taxonomyAdminService.deleteCategory(category.getId());
        assertFalse(category.getIsActive());
        verify(categoryRepository).save(category);
    }

    @Test
    void testCreateTag() {
        CreateTagRequest req = new CreateTagRequest();
        req.setSlug("new-tag");
        req.setName("New Tag");
        when(tagRepository.existsBySlug("new-tag")).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenAnswer(i -> i.getArgument(0));
        taxonomyAdminService.createTag(req);
        verify(tagRepository).save(any(Tag.class));
    }

    @Test
    void testUpdateTag() {
        CreateTagRequest req = new CreateTagRequest();
        req.setSlug("updated-tag");
        req.setName("Updated Tag");
        
        when(tagRepository.findById(tag.getId())).thenReturn(Optional.of(tag));
        when(tagRepository.existsBySlug("updated-tag")).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenAnswer(i -> i.getArgument(0));
        
        taxonomyAdminService.updateTag(tag.getId(), req);
        assertEquals("updated-tag", tag.getSlug());
    }

    @Test
    void testDeleteTag() {
        when(tagRepository.existsById(tag.getId())).thenReturn(true);
        taxonomyAdminService.deleteTag(tag.getId());
        verify(tagRepository).deleteById(tag.getId());
    }

    @Test
    void testMergeTags_SameTag() {
        TagMergeRequest req = new TagMergeRequest();
        req.setSourceTagId(tag.getId());
        req.setTargetTagId(tag.getId());
        assertThrows(BadRequestException.class, () -> taxonomyAdminService.mergeTags(req));
    }

    @Test
    void testMergeTags_Success() {
        Tag targetTag = new Tag();
        targetTag.setId(UUID.randomUUID());
        targetTag.setUsageCount(20);

        TagMergeRequest req = new TagMergeRequest();
        req.setSourceTagId(tag.getId());
        req.setTargetTagId(targetTag.getId());

        when(tagRepository.findById(tag.getId())).thenReturn(Optional.of(tag));
        when(tagRepository.findById(targetTag.getId())).thenReturn(Optional.of(targetTag));

        // Relation 1: only has source tag
        TemplateTag r1 = new TemplateTag();
        r1.setId(new TemplateTagId(UUID.randomUUID(), tag.getId()));
        r1.setTemplate(new Template());
        
        // Relation 2: already has target tag
        TemplateTag r2 = new TemplateTag();
        r2.setId(new TemplateTagId(UUID.randomUUID(), tag.getId()));
        r2.setTemplate(new Template());

        when(templateTagRepository.findByIdTagId(tag.getId())).thenReturn(List.of(r1, r2));
        
        when(templateTagRepository.existsByIdTemplateIdAndIdTagId(r1.getId().getTemplateId(), targetTag.getId())).thenReturn(false);
        when(templateTagRepository.existsByIdTemplateIdAndIdTagId(r2.getId().getTemplateId(), targetTag.getId())).thenReturn(true);

        taxonomyAdminService.mergeTags(req);

        verify(templateTagRepository).delete(r1);
        verify(templateTagRepository).delete(r2);
        verify(templateTagRepository).save(any(TemplateTag.class)); // For r1

        assertEquals(30, targetTag.getUsageCount());
        verify(tagRepository).save(targetTag);
        verify(tagRepository).delete(tag);
    }
}
