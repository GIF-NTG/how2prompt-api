package com.example.how2prompt.modules.catalog.controller;

import com.example.how2prompt.modules.catalog.dto.request.CreateAiModelRequest;
import com.example.how2prompt.modules.catalog.dto.request.UpdateAiModelRequest;
import com.example.how2prompt.modules.catalog.dto.response.AiModelResponse;
import com.example.how2prompt.modules.catalog.service.AiModelAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin/ai-models")
@PreAuthorize("hasRole('ADMIN')")
public class AiModelAdminController {

    private final AiModelAdminService aiModelAdminService;

    public AiModelAdminController(AiModelAdminService aiModelAdminService) {
        this.aiModelAdminService = aiModelAdminService;
    }

    @PostMapping
    public ResponseEntity<AiModelResponse> createAiModel(@Valid @RequestBody CreateAiModelRequest request) {
        AiModelResponse response = aiModelAdminService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AiModelResponse> updateAiModel(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAiModelRequest request) {
        AiModelResponse response = aiModelAdminService.update(id, request);
        return ResponseEntity.ok(response);
    }
}
