package com.example.how2prompt.modules.template.service;

import com.example.how2prompt.common.exception.BadRequestException;
import com.example.how2prompt.common.exception.ResourceNotFoundException;
import com.example.how2prompt.common.security.AuthenticatedUser;
import com.example.how2prompt.modules.template.dto.GeneratePromptRequest;
import com.example.how2prompt.modules.template.dto.GeneratePromptResponse;
import com.example.how2prompt.modules.template.dto.RenderResult;
import com.example.how2prompt.modules.template.entity.Template;
import com.example.how2prompt.modules.template.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Orchestrate generate (US-3.5–3.8):
 * <ol>
 *   <li>Kiểm tra quyền generate template</li>
 *   <li>Backend render = source of truth ({@link PromptRenderService})</li>
 *   <li>Trả kết quả ngay cho client (copy/preview)</li>
 *   <li>User đăng nhập: tăng usage và lưu history async; guest: không ghi dữ liệu</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromptGenerateService {

    private static final String STATUS_PUBLISHED = "published";

    private final PromptRenderService promptRenderService;
    private final TemplateUsageService templateUsageService;
    private final GeneratedPromptHistoryService generatedPromptHistoryService;
    private final TemplateRepository templateRepository;

    /**
     * Generate prompt cho template.
     *
     * @param templateId  id template
     * @param request     input form + model + extra instructions
     * @param currentUser user đang đăng nhập; {@code null} khi guest generate
     */
    public GeneratePromptResponse generate(
            UUID templateId,
            GeneratePromptRequest request,
            AuthenticatedUser currentUser
    ) {
        boolean authenticated = currentUser != null;
        if (authenticated) {
            requireUserContext(currentUser);
        }
        requireGenerateAccess(templateId, currentUser);

        Map<String, Object> inputValues = request.getInputValues() != null
                ? request.getInputValues()
                : Map.of();

        RenderResult render = promptRenderService.render(
                templateId,
                request.getAiModelId(),
                inputValues,
                request.getExtraInstructions()
        );

        if (authenticated) {
            try {
                templateUsageService.incrementUsageCount(templateId);
            } catch (Exception ex) {
                log.warn("Failed to increment usage_count for template {}: {}", templateId, ex.getMessage());
            }

            generatedPromptHistoryService.saveAsync(
                    currentUser.userId(),
                    currentUser.workspaceId(),
                    render,
                    new HashMap<>(inputValues),
                    request.getTitle()
            );
        }

        return GeneratePromptResponse.from(render, blankToNull(request.getTitle()));
    }

    private static void requireUserContext(AuthenticatedUser currentUser) {
        if (currentUser.userId() == null) {
            throw new BadRequestException("Authenticated user thiếu user_id.");
        }
        if (currentUser.workspaceId() == null) {
            throw new BadRequestException("Authenticated user thiếu workspace_id.");
        }
    }

    private void requireGenerateAccess(UUID templateId, AuthenticatedUser currentUser) {
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> ResourceNotFoundException.of("Template", templateId));

        boolean publiclyAvailable = template.isPublic()
                && STATUS_PUBLISHED.equals(template.getStatus());
        boolean sameWorkspace = currentUser != null
                && Objects.equals(template.getWorkspaceId(), currentUser.workspaceId());
        boolean admin = currentUser != null && currentUser.admin();

        if (!publiclyAvailable && !sameWorkspace && !admin) {
            // Do not reveal the existence of private templates across workspaces.
            throw ResourceNotFoundException.of("Template", templateId);
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
