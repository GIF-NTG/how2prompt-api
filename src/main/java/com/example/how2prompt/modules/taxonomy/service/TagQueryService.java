package com.example.how2prompt.modules.taxonomy.service;

import com.example.how2prompt.common.exception.ResourceNotFoundException;
import com.example.how2prompt.modules.taxonomy.dto.response.TagResponse;
import com.example.how2prompt.modules.taxonomy.entity.Tag;
import com.example.how2prompt.modules.taxonomy.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagQueryService {

    private final TagRepository tagRepository;

    /**
     * Lấy các tag phổ biến theo limit (sắp xếp theo usageCount giảm dần).
     */
    public List<TagResponse> findPopular(int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        return tagRepository.findPopular(PageRequest.of(0, limit)).stream()
                .map(this::mapToTagResponse)
                .collect(Collectors.toList());
    }

    /**
     * Resolve slug thành UUID.
     * Ném ResourceNotFoundException nếu không tìm thấy.
     */
    public UUID resolveIdBySlug(String slug) {
        if (slug == null || slug.isBlank()) {
            return null;
        }
        return tagRepository.findBySlug(slug)
                .map(Tag::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", slug));
    }

    /**
     * Resolve danh sách slugs thành danh sách UUIDs.
     */
    public List<UUID> resolveIdsBySlugs(List<String> slugs) {
        if (slugs == null || slugs.isEmpty()) {
            return Collections.emptyList();
        }
        return slugs.stream()
                .map(this::resolveIdBySlug)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách tag theo tập hợp IDs.
     */
    public List<TagResponse> findByIds(Set<UUID> ids) {
        return tagRepository.findAllById(ids).stream()
                .map(this::mapToTagResponse)
                .collect(Collectors.toList());
    }

    public TagResponse mapToTagResponse(Tag entity) {
        TagResponse response = new TagResponse();
        response.setId(entity.getId());
        response.setSlug(entity.getSlug());
        response.setName(entity.getName());
        response.setUsageCount(entity.getUsageCount());
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }
}
