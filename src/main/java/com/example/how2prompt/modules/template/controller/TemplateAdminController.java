package com.example.how2prompt.modules.template.controller;

import com.example.how2prompt.common.response.ApiResponse;
import com.example.how2prompt.common.security.AuthenticatedUser;
import com.example.how2prompt.common.security.CurrentUser;
import com.example.how2prompt.modules.template.dto.CreateTemplateRequest;
import com.example.how2prompt.modules.template.dto.CreateVariableRequest;
import com.example.how2prompt.modules.template.dto.CreateVariantRequest;
import com.example.how2prompt.modules.template.dto.TemplateResponse;
import com.example.how2prompt.modules.template.dto.TemplateVariableResponse;
import com.example.how2prompt.modules.template.dto.TemplateVariantResponse;
import com.example.how2prompt.modules.template.dto.UpdateTemplateRequest;
import com.example.how2prompt.modules.template.service.TemplateAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

/**
 * Admin CRUD template (Giai đoạn 2). Path prefix {@code /api/v1} gắn bởi WebConfig.
 */
@RestController
@RequestMapping("/admin/templates")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class TemplateAdminController {

    private final TemplateAdminService templateAdminService;

    @PostMapping
    public ResponseEntity<ApiResponse<TemplateResponse>> createTemplate(
            @Valid @RequestBody CreateTemplateRequest request,
            @CurrentUser AuthenticatedUser currentUser
    ) {
        TemplateResponse body = templateAdminService.createTemplate(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(body));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<TemplateResponse>> updateTemplate(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateTemplateRequest request
    ) {
        TemplateResponse body = templateAdminService.updateTemplate(id, request);
        return ResponseEntity.ok(ApiResponse.of(body));
    }

    @PostMapping("/{id}/variables")
    public ResponseEntity<ApiResponse<TemplateVariableResponse>> addVariable(
            @PathVariable("id") UUID id,
            @Valid @RequestBody CreateVariableRequest request
    ) {
        TemplateVariableResponse body = templateAdminService.addVariable(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(body));
    }

    @PostMapping("/{id}/variants")
    public ResponseEntity<ApiResponse<TemplateVariantResponse>> addVariant(
            @PathVariable("id") UUID id,
            @Valid @RequestBody CreateVariantRequest request
    ) {
        TemplateVariantResponse body = templateAdminService.addVariant(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(body));
    }
}
