package com.example.how2prompt.modules.catalog.service;

import com.example.how2prompt.common.exception.BadRequestException;
import com.example.how2prompt.common.exception.ResourceNotFoundException;
import com.example.how2prompt.modules.catalog.entity.AiModel;
import com.example.how2prompt.modules.catalog.repository.AiModelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiModelQueryServiceTest {

    @Mock
    private AiModelRepository aiModelRepository;

    @InjectMocks
    private AiModelQueryService aiModelQueryService;

    @Test
    void getByIdOrThrow_missingModel_throwsNotFound() {
        UUID id = UUID.randomUUID();
        when(aiModelRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aiModelQueryService.getByIdOrThrow(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getActiveByIdOrThrow_inactiveModel_throwsBadRequest() {
        UUID id = UUID.randomUUID();
        AiModel model = new AiModel();
        model.setId(id);
        model.setIsActive(false);
        when(aiModelRepository.findById(id)).thenReturn(Optional.of(model));

        assertThatThrownBy(() -> aiModelQueryService.getActiveByIdOrThrow(id))
                .isInstanceOf(BadRequestException.class)
                .satisfies(ex -> assertThat(((BadRequestException) ex).getDetails())
                        .containsEntry("aiModelId", id));
    }
}
