package com.example.how2prompt.modules.prompt.controller;

import com.example.how2prompt.common.response.ApiResponse;
import com.example.how2prompt.common.response.PageResponse;
import com.example.how2prompt.common.security.AuthenticatedUser;
import com.example.how2prompt.common.security.CurrentUser;
import com.example.how2prompt.modules.prompt.dto.response.PromptHistoryDetailResponse;
import com.example.how2prompt.modules.prompt.dto.response.PromptHistoryResponse;
import com.example.how2prompt.modules.prompt.service.GeneratedPromptHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * History & management API cho generated prompts (Epic 4).
 * <p>
 * Path prefix {@code /api/v1} gắn bởi WebConfig.
 */
@RestController
@RequestMapping("/generated-prompts")
@RequiredArgsConstructor
public class GeneratedPromptController {

    private final GeneratedPromptHistoryService generatedPromptHistoryService;

    /**
     * GET /api/v1/generated-prompts — US-4.2 (View personal history).
     * Supports optional filters: templateId (US-4.3/4.4), aiModelId, search.
     * Uses cursor-based pagination consistent with template search.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PromptHistoryResponse>>> getHistory(
            @CurrentUser AuthenticatedUser currentUser,
            @RequestParam(required = false) UUID templateId,
            @RequestParam(required = false) UUID aiModelId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit
    ) {
        PageResponse<PromptHistoryResponse> body = generatedPromptHistoryService.getHistory(
                currentUser.userId(),
                templateId,
                aiModelId,
                search,
                cursor,
                limit
        );
        return ResponseEntity.ok(ApiResponse.of(body));
    }

    /**
     * GET /api/v1/generated-prompts/{id} — US-4.3 (Reload/view detail).
     * Returns full input_values for re-run form pre-fill.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PromptHistoryDetailResponse>> getById(
            @PathVariable UUID id,
            @CurrentUser AuthenticatedUser currentUser
    ) {
        PromptHistoryDetailResponse body = generatedPromptHistoryService.getById(id, currentUser.userId());
        return ResponseEntity.ok(ApiResponse.of(body));
    }

    /**
     * DELETE /api/v1/generated-prompts/{id} — US-4.5 (Soft delete).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @CurrentUser AuthenticatedUser currentUser
    ) {
        generatedPromptHistoryService.delete(id, currentUser.userId());
        return ResponseEntity.noContent().build();
    }
}
