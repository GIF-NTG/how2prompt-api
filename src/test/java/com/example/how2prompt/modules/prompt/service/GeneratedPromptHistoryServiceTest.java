package com.example.how2prompt.modules.prompt.service;

import com.example.how2prompt.common.exception.ForbiddenException;
import com.example.how2prompt.common.response.PageResponse;
import com.example.how2prompt.modules.prompt.dto.response.PromptHistoryDetailResponse;
import com.example.how2prompt.modules.prompt.dto.response.PromptHistoryResponse;
import com.example.how2prompt.modules.prompt.entity.GeneratedPrompt;
import com.example.how2prompt.modules.prompt.exception.GeneratedPromptNotFoundException;
import com.example.how2prompt.modules.prompt.repository.GeneratedPromptRepository;
import com.example.how2prompt.modules.template.dto.RenderResult;
import com.example.how2prompt.modules.template.dto.TemplateVersionStatus;
import com.example.how2prompt.modules.template.service.TemplateQueryService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GeneratedPromptHistoryServiceTest {

    private final GeneratedPromptRepository generatedPromptRepository =
            mock(GeneratedPromptRepository.class);
    private final TemplateQueryService templateQueryService =
            mock(TemplateQueryService.class);
    private final GeneratedPromptHistoryService generatedPromptHistoryService =
            new GeneratedPromptHistoryService(generatedPromptRepository, templateQueryService);

    @Test
    void saveAsyncMapsAuthenticatedGenerateHistory() {
        UUID userId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        Map<String, Object> inputValues = Map.of("topic", "AI");
        RenderResult render = new RenderResult(
                templateId,
                versionId,
                null,
                "Rendered prompt",
                null,
                false,
                inputValues,
                "Be concise"
        );

        generatedPromptHistoryService.saveAsync(
                userId,
                workspaceId,
                render,
                inputValues,
                "  Saved title  "
        );

        ArgumentCaptor<GeneratedPrompt> captor = ArgumentCaptor.forClass(GeneratedPrompt.class);
        verify(generatedPromptRepository).save(captor.capture());
        GeneratedPrompt saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getWorkspaceId()).isEqualTo(workspaceId);
        assertThat(saved.getTemplateId()).isEqualTo(templateId);
        assertThat(saved.getTemplateVersionId()).isEqualTo(versionId);
        assertThat(saved.getInputValues()).isEqualTo(inputValues);
        assertThat(saved.getExtraInstructions()).isEqualTo("Be concise");
        assertThat(saved.getFinalPrompt()).isEqualTo("Rendered prompt");
        assertThat(saved.getTitle()).isEqualTo("Saved title");
    }

    @Test
    @SuppressWarnings("unchecked")
    void getHistory_returnsPagedResults() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();
        UUID aiModelId = UUID.randomUUID();
        String search = "test";
        String cursor = null;
        int limit = 10;

        GeneratedPrompt prompt = new GeneratedPrompt();
        prompt.setId(UUID.randomUUID());
        prompt.setUserId(userId);
        prompt.setTemplateId(templateId);
        prompt.setAiModelId(aiModelId);
        prompt.setFinalPrompt("rendered text");

        Page<GeneratedPrompt> page = new PageImpl<>(List.of(prompt));
        when(generatedPromptRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        // Act
        PageResponse<PromptHistoryResponse> response = generatedPromptHistoryService.getHistory(
                userId, templateId, aiModelId, search, cursor, limit
        );

        // Assert
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).templateId()).isEqualTo(templateId);
        verify(generatedPromptRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getById_shouldReturnDetails_whenFoundAndAuthorized() {
        // Arrange
        UUID promptId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();

        GeneratedPrompt prompt = new GeneratedPrompt();
        prompt.setId(promptId);
        prompt.setUserId(userId);
        prompt.setTemplateId(templateId);
        prompt.setTemplateVersionId(versionId);
        prompt.setInputValues(Map.of("q", "a"));
        prompt.setFinalPrompt("rendered text");

        TemplateVersionStatus mockStatus = new TemplateVersionStatus(false, true, "v2");

        when(generatedPromptRepository.findById(promptId)).thenReturn(Optional.of(prompt));
        when(templateQueryService.checkTemplateVersionStatus(templateId, versionId)).thenReturn(mockStatus);

        // Act
        PromptHistoryDetailResponse response = generatedPromptHistoryService.getById(promptId, userId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(promptId);
        assertThat(response.templateDeleted()).isFalse();
        assertThat(response.newerVersionAvailable()).isTrue();
        assertThat(response.latestVersionNumber()).isEqualTo("v2");
        verify(generatedPromptRepository).findById(promptId);
        verify(templateQueryService).checkTemplateVersionStatus(templateId, versionId);
    }

    @Test
    void getById_shouldThrowForbiddenException_whenNotOwner() {
        // Arrange
        UUID promptId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID accessorId = UUID.randomUUID();

        GeneratedPrompt prompt = new GeneratedPrompt();
        prompt.setId(promptId);
        prompt.setUserId(ownerId);

        when(generatedPromptRepository.findById(promptId)).thenReturn(Optional.of(prompt));

        // Act & Assert
        assertThatThrownBy(() -> generatedPromptHistoryService.getById(promptId, accessorId))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void getById_shouldThrowNotFoundException_whenNotFound() {
        // Arrange
        UUID promptId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(generatedPromptRepository.findById(promptId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> generatedPromptHistoryService.getById(promptId, userId))
                .isInstanceOf(GeneratedPromptNotFoundException.class);
    }

    @Test
    void delete_shouldSoftDelete_whenOwner() {
        // Arrange
        UUID promptId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        GeneratedPrompt prompt = new GeneratedPrompt();
        prompt.setId(promptId);
        prompt.setUserId(userId);
        prompt.setDeletedAt(null);

        when(generatedPromptRepository.findById(promptId)).thenReturn(Optional.of(prompt));

        // Act
        generatedPromptHistoryService.delete(promptId, userId);

        // Assert
        ArgumentCaptor<GeneratedPrompt> captor = ArgumentCaptor.forClass(GeneratedPrompt.class);
        verify(generatedPromptRepository).save(captor.capture());
        GeneratedPrompt saved = captor.getValue();
        assertThat(saved.getDeletedAt()).isNotNull();
        verify(generatedPromptRepository).findById(promptId);
    }

    @Test
    void delete_shouldThrowForbiddenException_whenNotOwner() {
        // Arrange
        UUID promptId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID accessorId = UUID.randomUUID();

        GeneratedPrompt prompt = new GeneratedPrompt();
        prompt.setId(promptId);
        prompt.setUserId(ownerId);

        when(generatedPromptRepository.findById(promptId)).thenReturn(Optional.of(prompt));

        // Act & Assert
        assertThatThrownBy(() -> generatedPromptHistoryService.delete(promptId, accessorId))
                .isInstanceOf(ForbiddenException.class);
    }
}
