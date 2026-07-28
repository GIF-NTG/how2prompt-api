package com.example.how2prompt.modules.template.service;

import com.example.how2prompt.common.exception.ResourceNotFoundException;
import com.example.how2prompt.modules.template.repository.TemplateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TemplateUsageServiceTest {

    @Mock
    private TemplateRepository templateRepository;

    @InjectMocks
    private TemplateUsageService templateUsageService;

    @Test
    void incrementUsageCount_delegatesToAtomicUpdate() {
        UUID id = UUID.randomUUID();
        when(templateRepository.incrementUsageCount(id)).thenReturn(1);

        templateUsageService.incrementUsageCount(id);

        verify(templateRepository).incrementUsageCount(id);
    }

    @Test
    void incrementUsageCount_whenNoRow_throwsNotFound() {
        UUID id = UUID.randomUUID();
        when(templateRepository.incrementUsageCount(id)).thenReturn(0);

        assertThatThrownBy(() -> templateUsageService.incrementUsageCount(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
