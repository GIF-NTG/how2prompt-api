package com.example.how2prompt.modules.template.controller;

import com.example.how2prompt.common.response.ApiResponse;
import com.example.how2prompt.common.security.AuthenticatedUser;
import com.example.how2prompt.common.security.CurrentUser;
import com.example.how2prompt.modules.template.dto.GeneratePromptRequest;
import com.example.how2prompt.modules.template.dto.GeneratePromptResponse;
import com.example.how2prompt.modules.template.service.GuestGenerateQuotaService;
import com.example.how2prompt.modules.template.service.PromptGenerateService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Generate prompt API (Epic 3 — US-3.5, US-3.6, US-3.8).
 * <p>
 * Path prefix {@code /api/v1} gắn bởi WebConfig.
 * Copy-to-clipboard (US-3.7) là client-side — response trả {@code finalPrompt} sẵn để copy.
 */
@RestController
@RequestMapping("/templates")
@RequiredArgsConstructor
public class PromptGenerateController {

    private final PromptGenerateService promptGenerateService;
    private final GuestGenerateQuotaService guestGenerateQuotaService;

    /**
     * POST /api/v1/templates/{id}/generate
     * <p>
     * Backend render source of truth. Guest chỉ nhận kết quả render; user đăng nhập
     * được tăng usage_count và lưu history bất đồng bộ.
     */
    @PostMapping("/{id}/generate")
    public ResponseEntity<ApiResponse<GeneratePromptResponse>> generate(
            @PathVariable("id") UUID id,
            @Valid @RequestBody GeneratePromptRequest request,
            @CurrentUser AuthenticatedUser currentUser,
            HttpServletRequest httpRequest
    ) {
        if (currentUser == null) {
            guestGenerateQuotaService.checkAndConsume(id, httpRequest.getRemoteAddr());
        }
        GeneratePromptResponse body = promptGenerateService.generate(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.of(body));
    }
}
