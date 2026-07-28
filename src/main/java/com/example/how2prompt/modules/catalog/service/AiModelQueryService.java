package com.example.how2prompt.modules.catalog.service;

import com.example.how2prompt.common.exception.ResourceNotFoundException;
import com.example.how2prompt.modules.catalog.dto.response.AiModelSummaryResponse;
import com.example.how2prompt.modules.catalog.entity.AiModel;
import com.example.how2prompt.modules.catalog.repository.AiModelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AiModelQueryService {

    private final AiModelRepository aiModelRepository;

    public AiModelQueryService(AiModelRepository aiModelRepository) {
        this.aiModelRepository = aiModelRepository;
    }

    public List<AiModelSummaryResponse> findActive() {
        return aiModelRepository.findByIsActiveTrue().stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    public List<AiModelSummaryResponse> findByIds(Set<UUID> ids) {
        return aiModelRepository.findAllById(ids).stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    public AiModelSummaryResponse getByIdOrThrow(UUID id) {
        AiModel aiModel = aiModelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AiModel", id));
        return mapToSummary(aiModel);
    }

    public UUID resolveIdByCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return aiModelRepository.findByCode(code)
                .map(AiModel::getId)
                .orElseThrow(() -> new ResourceNotFoundException("AiModel", code));
    }
    
    public List<AiModelSummaryResponse> findAll() {
        return aiModelRepository.findAll().stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    private AiModelSummaryResponse mapToSummary(AiModel entity) {
        return new AiModelSummaryResponse(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getIconUrl()
        );
    }
}
