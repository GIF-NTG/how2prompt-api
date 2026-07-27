package com.example.how2prompt.modules.catalog.controller;

import com.example.how2prompt.modules.catalog.dto.response.AiModelSummaryResponse;
import com.example.how2prompt.modules.catalog.service.AiModelQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ai-models")
public class AiModelController {

    private final AiModelQueryService aiModelQueryService;

    public AiModelController(AiModelQueryService aiModelQueryService) {
        this.aiModelQueryService = aiModelQueryService;
    }

    @GetMapping
    public ResponseEntity<List<AiModelSummaryResponse>> getAiModels(Authentication authentication) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));

        if (isAdmin) {
            return ResponseEntity.ok(aiModelQueryService.findAll());
        } else {
            return ResponseEntity.ok(aiModelQueryService.findActive());
        }
    }
}
