package com.example.how2prompt.modules.prompt.controller;

import com.example.how2prompt.common.security.AuthenticatedUser;
import com.example.how2prompt.modules.prompt.service.GeneratedPromptHistoryService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.mock;

/**
 * Unit tests cho {@link GeneratedPromptController}.
 * <p>
 * Endpoint stubs chưa implement — test fixtures sẵn sàng, đánh dấu {@code @Disabled}
 * cho tới khi service layer hoàn thành (Day 3).
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
    @Disabled("Pending Day 3 — service methods not yet implemented")
    void getHistory_shouldReturnPagedResults() {
        // Arrange: stub historyService.getHistory(...)
        // Act:     controller.getHistory(testUser, null, null, null, pageable)
        // Assert:  response status 200, body contains paged results
    }

    @Test
    @Disabled("Pending Day 3 — service methods not yet implemented")
    void getById_shouldReturn200_whenFound() {
        // Arrange: stub historyService.getById(...)
        // Act:     controller.getById(promptId, testUser)
        // Assert:  response status 200, body matches expected prompt
    }

    @Test
    @Disabled("Pending Day 3 — service methods not yet implemented")
    void getById_shouldReturn404_whenNotFound() {
        // Arrange: stub historyService.getById(...) to throw GeneratedPromptNotFoundException
        // Act:     controller.getById(unknownId, testUser)
        // Assert:  throws GeneratedPromptNotFoundException
    }

    @Test
    @Disabled("Pending Day 3 — service methods not yet implemented")
    void delete_shouldReturn204() {
        // Arrange: stub historyService.delete(...)
        // Act:     controller.delete(promptId, testUser)
        // Assert:  response status 204
    }
}
