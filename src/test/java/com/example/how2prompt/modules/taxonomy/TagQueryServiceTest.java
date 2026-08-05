package com.example.how2prompt.modules.taxonomy;

import com.example.how2prompt.common.exception.ResourceNotFoundException;
import com.example.how2prompt.modules.taxonomy.dto.response.TagResponse;
import com.example.how2prompt.modules.taxonomy.entity.Tag;
import com.example.how2prompt.modules.taxonomy.repository.TagRepository;
import com.example.how2prompt.modules.taxonomy.service.TagQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagQueryServiceTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagQueryService tagQueryService;

    private Tag tag;

    @BeforeEach
    void setUp() {
        tag = new Tag();
        tag.setId(UUID.randomUUID());
        tag.setSlug("test-tag");
        tag.setName("Test Tag");
    }

    @Test
    void testFindPopular_ValidLimit() {
        when(tagRepository.findPopular(any(Pageable.class))).thenReturn(List.of(tag));
        List<TagResponse> res = tagQueryService.findPopular(5);
        assertEquals(1, res.size());
    }

    @Test
    void testFindPopular_InvalidLimit() {
        List<TagResponse> res = tagQueryService.findPopular(0);
        assertTrue(res.isEmpty());
        
        List<TagResponse> res2 = tagQueryService.findPopular(-1);
        assertTrue(res2.isEmpty());
    }

    @Test
    void testResolveIdBySlug_NullOrBlank() {
        assertNull(tagQueryService.resolveIdBySlug(null));
        assertNull(tagQueryService.resolveIdBySlug("  "));
    }

    @Test
    void testResolveIdBySlug_Success() {
        when(tagRepository.findBySlug("test-tag")).thenReturn(Optional.of(tag));
        assertEquals(tag.getId(), tagQueryService.resolveIdBySlug("test-tag"));
    }

    @Test
    void testResolveIdBySlug_NotFound() {
        when(tagRepository.findBySlug("unknown")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> tagQueryService.resolveIdBySlug("unknown"));
    }

    @Test
    void testResolveIdsBySlugs_NullOrEmpty() {
        assertTrue(tagQueryService.resolveIdsBySlugs(null).isEmpty());
        assertTrue(tagQueryService.resolveIdsBySlugs(List.of()).isEmpty());
    }

    @Test
    void testResolveIdsBySlugs_Success() {
        when(tagRepository.findBySlug("test-tag")).thenReturn(Optional.of(tag));
        List<UUID> ids = tagQueryService.resolveIdsBySlugs(List.of("test-tag"));
        assertEquals(1, ids.size());
        assertEquals(tag.getId(), ids.get(0));
    }

    @Test
    void testFindByIds() {
        when(tagRepository.findAllById(Set.of(tag.getId()))).thenReturn(List.of(tag));
        List<TagResponse> res = tagQueryService.findByIds(Set.of(tag.getId()));
        assertEquals(1, res.size());
    }
}
