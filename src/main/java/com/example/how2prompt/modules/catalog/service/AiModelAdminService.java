package com.example.how2prompt.modules.catalog.service;

import com.example.how2prompt.modules.catalog.dto.request.CreateAiModelRequest;
import com.example.how2prompt.modules.catalog.dto.request.UpdateAiModelRequest;
import com.example.how2prompt.modules.catalog.dto.response.AiModelResponse;
import com.example.how2prompt.modules.catalog.entity.AiModel;
import com.example.how2prompt.modules.catalog.repository.AiModelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class AiModelAdminService {

    private final AiModelRepository aiModelRepository;

    public AiModelAdminService(AiModelRepository aiModelRepository) {
        this.aiModelRepository = aiModelRepository;
    }

    public AiModelResponse create(CreateAiModelRequest request) {
        if (aiModelRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("AiModel code already exists: " + request.getCode());
        }

        AiModel model = new AiModel();
        model.setCode(request.getCode());
        model.setName(request.getName());
        model.setProvider(request.getProvider());
        model.setModelType(request.getModelType());
        model.setDescription(request.getDescription());
        model.setCapabilities(request.getCapabilities() != null ? request.getCapabilities() : java.util.Map.of());
        model.setDefaultConfig(request.getDefaultConfig() != null ? request.getDefaultConfig() : java.util.Map.of());
        model.setIconUrl(request.getIconUrl());
        model.setDocUrl(request.getDocUrl());
        
        if (request.getIsActive() != null) {
            model.setIsActive(request.getIsActive());
        }
        
        if (request.getSortOrder() != null) {
            model.setSortOrder(request.getSortOrder());
        }


        AiModel saved = aiModelRepository.save(model);
        return mapToResponse(saved);
    }

    public AiModelResponse update(UUID id, UpdateAiModelRequest request) {
        AiModel model = aiModelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AiModel not found with id: " + id));

        if (request.getName() != null) {
            model.setName(request.getName());
        }
        if (request.getProvider() != null) {
            model.setProvider(request.getProvider());
        }
        if (request.getModelType() != null) {
            model.setModelType(request.getModelType());
        }
        if (request.getDescription() != null) {
            model.setDescription(request.getDescription());
        }
        if (request.getCapabilities() != null) {
            model.setCapabilities(request.getCapabilities());
        }
        if (request.getDefaultConfig() != null) {
            model.setDefaultConfig(request.getDefaultConfig());
        }
        if (request.getIconUrl() != null) {
            model.setIconUrl(request.getIconUrl());
        }
        if (request.getDocUrl() != null) {
            model.setDocUrl(request.getDocUrl());
        }
        if (request.getIsActive() != null) {
            model.setIsActive(request.getIsActive());
        }
        if (request.getSortOrder() != null) {
            model.setSortOrder(request.getSortOrder());
        }

        
        AiModel updated = aiModelRepository.save(model);
        return mapToResponse(updated);
    }

    private AiModelResponse mapToResponse(AiModel entity) {
        AiModelResponse res = new AiModelResponse();
        res.setId(entity.getId());
        res.setCode(entity.getCode());
        res.setName(entity.getName());
        res.setProvider(entity.getProvider());
        res.setModelType(entity.getModelType());
        res.setDescription(entity.getDescription());
        res.setCapabilities(entity.getCapabilities());
        res.setDefaultConfig(entity.getDefaultConfig());
        res.setIconUrl(entity.getIconUrl());
        res.setDocUrl(entity.getDocUrl());
        res.setIsActive(entity.getIsActive());
        res.setSortOrder(entity.getSortOrder());
        res.setCreatedAt(entity.getCreatedAt());
        res.setUpdatedAt(entity.getUpdatedAt());
        return res;
    }
}
