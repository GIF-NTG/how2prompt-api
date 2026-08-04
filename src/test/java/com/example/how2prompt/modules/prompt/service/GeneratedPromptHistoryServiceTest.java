package com.example.how2prompt.modules.prompt.service;

import com.example.how2prompt.common.exception.BadRequestException;
import com.example.how2prompt.common.exception.ForbiddenException;
import com.example.how2prompt.common.exception.BadRequestException;
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
    void saveMapsAuthenticatedGenerateHistory() {
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

        GeneratedPrompt mockSaved = new GeneratedPrompt();
        UUID promptId = UUID.randomUUID();
        mockSaved.setId(promptId);
        when(generatedPromptRepository.save(any(GeneratedPrompt.class))).thenReturn(mockSaved);

        GeneratedPrompt result = generatedPromptHistoryService.save(
                userId,
                workspaceId,
                render,
                inputValues,
                "  Saved title  "
        );

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(promptId);

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

    @Test
    void getHistory_userIdNull_throwsBadRequest() {
        assertThatThrownBy(() -> generatedPromptHistoryService.getHistory(null, null, null, null, null, 10))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void getById_idOrUserIdNull_throwsBadRequest() {
        assertThatThrownBy(() -> generatedPromptHistoryService.getById(null, UUID.randomUUID()))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> generatedPromptHistoryService.getById(UUID.randomUUID(), null))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void delete_idOrUserIdNull_throwsBadRequest() {
        assertThatThrownBy(() -> generatedPromptHistoryService.delete(null, UUID.randomUUID()))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> generatedPromptHistoryService.delete(UUID.randomUUID(), null))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void getHistory_invalidLimit_resolvesToDefaultLimit() {
        UUID userId = UUID.randomUUID();
        Page<GeneratedPrompt> page = new PageImpl<>(List.of());
        when(generatedPromptRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        // limit < 1
        generatedPromptHistoryService.getHistory(userId, null, null, null, null, 0);
        // limit > 100
        generatedPromptHistoryService.getHistory(userId, null, null, null, null, 150);

        // It should request size DEFAULT_LIMIT + 1 = 21
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(generatedPromptRepository, times(2)).findAll(any(Specification.class), captor.capture());
        assertThat(captor.getAllValues().get(0).getPageSize()).isEqualTo(21);
        assertThat(captor.getAllValues().get(1).getPageSize()).isEqualTo(21);
    }

    @Test
    void getHistory_hasMore_generatesNextCursor() {
        UUID userId = UUID.randomUUID();
        
        GeneratedPrompt p1 = new GeneratedPrompt();
        p1.setId(UUID.randomUUID());
        p1.setCreatedAt(Instant.parse("2024-01-02T10:00:00Z"));
        
        GeneratedPrompt p2 = new GeneratedPrompt();
        p2.setId(UUID.randomUUID());
        p2.setCreatedAt(Instant.parse("2024-01-01T10:00:00Z"));
        
        // request limit = 1, repo returns 2 (hasMore = true)
        Page<GeneratedPrompt> page = new PageImpl<>(List.of(p1, p2));
        when(generatedPromptRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        PageResponse<PromptHistoryResponse> response = generatedPromptHistoryService.getHistory(
                userId, null, null, null, null, 1
        );

        assertThat(response.isHasMore()).isTrue();
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getNextCursor()).isNotNull();
    }

    @Test
    void save_nullTitleAndInputs_savedGracefully() {
        RenderResult render = new RenderResult(
                UUID.randomUUID(), UUID.randomUUID(), null, "text", null, false, Map.of(), null
        );

        when(generatedPromptRepository.save(any(GeneratedPrompt.class))).thenAnswer(i -> i.getArgument(0));

        GeneratedPrompt result = generatedPromptHistoryService.save(
                UUID.randomUUID(), UUID.randomUUID(), render, null, "   "
        );

        assertThat(result.getTitle()).isNull();
        assertThat(result.getInputValues()).isEmpty();
    }
}
