package com.example.how2prompt.modules.template.service;

import com.example.how2prompt.common.exception.ResourceNotFoundException;
import com.example.how2prompt.modules.template.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Cập nhật usage_count atomic — tránh lost update khi nhiều user generate song song.
 */
@Service
@RequiredArgsConstructor
public class TemplateUsageService {

    private final TemplateRepository templateRepository;

    /**
     * Tăng {@code usage_count} lên 1 bằng bulk UPDATE (không load-rồi-save).
     *
     * @param templateId id template
     * @throws ResourceNotFoundException nếu template không tồn tại (hoặc soft-deleted)
     */
    @Transactional
    public void incrementUsageCount(UUID templateId) {
        int updated = templateRepository.incrementUsageCount(templateId);
        if (updated == 0) {
            throw ResourceNotFoundException.of("Template", templateId);
        }
    }
}
