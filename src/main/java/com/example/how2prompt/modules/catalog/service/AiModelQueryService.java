package com.example.how2prompt.modules.catalog.service;

import com.example.how2prompt.common.exception.ResourceNotFoundException;
import com.example.how2prompt.modules.catalog.dto.AiModelView;
import com.example.how2prompt.modules.catalog.entity.AiModel;
import com.example.how2prompt.modules.catalog.repository.AiModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Public query API của module catalog (US-5.1).
 * Module template/prompt chỉ được phụ thuộc service này — không import entity/repository.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiModelQueryService {

    private final AiModelRepository aiModelRepository;

    /**
     * Lấy AI model theo id; ném {@link ResourceNotFoundException} nếu không tồn tại.
     */
    public AiModelView getByIdOrThrow(UUID id) {
        AiModel model = aiModelRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("AiModel", id));
        return toView(model);
    }

    private static AiModelView toView(AiModel model) {
        return new AiModelView(
                model.getId(),
                model.getCode(),
                model.getName(),
                model.getProvider(),
                model.getModelType(),
                model.isActive()
        );
    }
}
