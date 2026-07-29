package com.example.how2prompt.modules.prompt.controller;

import com.example.how2prompt.common.response.ApiResponse;
import com.example.how2prompt.common.response.PageResponse;
import com.example.how2prompt.common.security.AuthenticatedUser;
import com.example.how2prompt.modules.prompt.dto.response.PromptHistoryDetailResponse;
import com.example.how2prompt.modules.prompt.dto.response.PromptHistoryResponse;
import com.example.how2prompt.modules.prompt.exception.GeneratedPromptNotFoundException;
import com.example.how2prompt.modules.prompt.service.GeneratedPromptHistoryService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests cho {@link GeneratedPromptController}.
 */
class GeneratedPromptControllerTest {

    private final GeneratedPromptHistoryService historyService =
            mock(GeneratedPromptHistoryService.class);
    private final GeneratedPromptController controller =
            new GeneratedPromptController(historyService);

    private final AuthenticatedUser testUser = new AuthenticatedUser(
            UUID.randomUUID(),
            "test@example.com",
            UUID.randomUUID(),
            false
    );

    @Test
    void getHistory_shouldReturnPagedResults() {
        // Arrange
        UUID templateId = UUID.randomUUID();
        UUID aiModelId = UUID.randomUUID();
        String search = "test";
        String cursor = "cursorString";
        int limit = 10;

        PageResponse<PromptHistoryResponse> mockPage = new PageResponse<>(
                Collections.emptyList(),
                "nextCursor",
                false
        );

        when(historyService.getHistory(testUser.userId(), templateId, aiModelId, search, cursor, limit))
                .thenReturn(mockPage);

        // Act
        ResponseEntity<ApiResponse<PageResponse<PromptHistoryResponse>>> response =
                controller.getHistory(testUser, templateId, aiModelId, search, cursor, limit);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data()).isEqualTo(mockPage);
        verify(historyService).getHistory(testUser.userId(), templateId, aiModelId, search, cursor, limit);
    }

    @Test
    void getById_shouldReturn200_whenFound() {
        // Arrange
        UUID promptId = UUID.randomUUID();
        PromptHistoryDetailResponse mockDetail = new PromptHistoryDetailResponse(
                promptId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Title",
                Map.of("key", "val"),
                "extra",
                "final prompt",
                Instant.now(),
                false,
                false,
                null
        );

        when(historyService.getById(promptId, testUser.userId())).thenReturn(mockDetail);

        // Act
        ResponseEntity<ApiResponse<PromptHistoryDetailResponse>> response =
                controller.getById(promptId, testUser);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data()).isEqualTo(mockDetail);
        verify(historyService).getById(promptId, testUser.userId());
    }

    @Test
    void getById_shouldReturn404_whenNotFound() {
        // Arrange
        UUID unknownId = UUID.randomUUID();
        when(historyService.getById(unknownId, testUser.userId()))
                .thenThrow(GeneratedPromptNotFoundException.of(unknownId));

        // Act & Assert
        assertThatThrownBy(() -> controller.getById(unknownId, testUser))
                .isInstanceOf(GeneratedPromptNotFoundException.class);
        verify(historyService).getById(unknownId, testUser.userId());
    }

    @Test
    void delete_shouldReturn204() {
        // Arrange
        UUID promptId = UUID.randomUUID();
        doNothing().when(historyService).delete(promptId, testUser.userId());

        // Act
        ResponseEntity<Void> response = controller.delete(promptId, testUser);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(historyService).delete(promptId, testUser.userId());
    }
}
