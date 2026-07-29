package com.example.how2prompt.modules.template.service;

import com.example.how2prompt.common.exception.ResourceNotFoundException;
import com.example.how2prompt.common.security.AuthenticatedUser;
import com.example.how2prompt.modules.template.dto.GeneratePromptRequest;
import com.example.how2prompt.modules.template.dto.GeneratePromptResponse;
import com.example.how2prompt.modules.template.dto.RenderResult;
import com.example.how2prompt.modules.template.entity.Template;
import com.example.how2prompt.modules.template.repository.TemplateRepository;
import com.example.how2prompt.modules.prompt.service.GeneratedPromptHistoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromptGenerateServiceTest {

    @Mock
    private PromptRenderService promptRenderService;
    @Mock
    private TemplateUsageService templateUsageService;
    @Mock
    private GeneratedPromptHistoryService generatedPromptHistoryService;
    @Mock
    private TemplateRepository templateRepository;

    @InjectMocks
    private PromptGenerateService promptGenerateService;

    @Test
    void generate_renders_incrementsUsage_andSavesHistoryAsync() {
        UUID templateId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        UUID modelId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();

        AuthenticatedUser user = new AuthenticatedUser(userId, "u@example.com", workspaceId, false);

        GeneratePromptRequest request = new GeneratePromptRequest();
        request.setAiModelId(modelId);
        request.setInputValues(Map.of("topic", "AI"));
        request.setExtraInstructions("Be concise");
        request.setTitle("My prompt");
        stubTemplate(templateId, workspaceId, false, "draft");

        RenderResult render = new RenderResult(
                templateId,
                versionId,
                modelId,
                "Write about AI\n\nBe concise",
                "sys",
                true,
                Map.of("topic", "AI"),
                "Be concise"
        );

        when(promptRenderService.render(templateId, modelId, request.getInputValues(), "Be concise"))
                .thenReturn(render);
        doNothing().when(templateUsageService).incrementUsageCount(templateId);

        GeneratePromptResponse response = promptGenerateService.generate(templateId, request, user);

        assertThat(response.finalPrompt()).isEqualTo("Write about AI\n\nBe concise");
        assertThat(response.usedVariant()).isTrue();
        assertThat(response.title()).isEqualTo("My prompt");
        assertThat(response.templateVersionId()).isEqualTo(versionId);

        verify(promptRenderService).render(templateId, modelId, request.getInputValues(), "Be concise");
        verify(templateUsageService).incrementUsageCount(templateId);
        verify(generatedPromptHistoryService).saveAsync(
                eq(userId),
                eq(workspaceId),
                eq(render),
                eq(Map.of("topic", "AI")),
                eq("My prompt")
        );
    }

    @Test
    void generate_withoutModel_skipsCatalogLookup() {
        UUID templateId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        AuthenticatedUser user = new AuthenticatedUser(userId, "u@example.com", workspaceId, false);

        GeneratePromptRequest request = new GeneratePromptRequest();
        request.setInputValues(Map.of("name", "Ada"));
        stubTemplate(templateId, workspaceId, false, "draft");

        RenderResult render = new RenderResult(
                templateId,
                UUID.randomUUID(),
                null,
                "Hello Ada",
                null,
                false,
                Map.of("name", "Ada"),
                null
        );

        when(promptRenderService.render(eq(templateId), isNull(), eq(request.getInputValues()), isNull()))
                .thenReturn(render);
        doNothing().when(templateUsageService).incrementUsageCount(templateId);

        GeneratePromptResponse response = promptGenerateService.generate(templateId, request, user);

        assertThat(response.finalPrompt()).isEqualTo("Hello Ada");
        assertThat(response.usedVariant()).isFalse();
        verify(generatedPromptHistoryService).saveAsync(
                eq(userId),
                eq(workspaceId),
                eq(render),
                eq(Map.of("name", "Ada")),
                isNull()
        );
    }

    @Test
    void generate_guestRendersPublicTemplateWithoutPersistenceSideEffects() {
        UUID templateId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        stubTemplate(templateId, UUID.randomUUID(), true, "published");

        GeneratePromptRequest request = new GeneratePromptRequest();
        request.setInputValues(Map.of("name", "Guest"));

        RenderResult render = new RenderResult(
                templateId,
                versionId,
                null,
                "Hello Guest",
                null,
                false,
                Map.of("name", "Guest"),
                null
        );
        when(promptRenderService.render(templateId, null, request.getInputValues(), null))
                .thenReturn(render);

        GeneratePromptResponse response = promptGenerateService.generate(templateId, request, null);

        assertThat(response.finalPrompt()).isEqualTo("Hello Guest");
        verifyNoInteractions(templateUsageService, generatedPromptHistoryService);
    }

    @Test
    void generate_guestCannotRenderPrivateTemplate() {
        UUID templateId = UUID.randomUUID();
        stubTemplate(templateId, UUID.randomUUID(), false, "draft");

        GeneratePromptRequest request = new GeneratePromptRequest();
        request.setInputValues(Map.of());

        assertThatThrownBy(() -> promptGenerateService.generate(templateId, request, null))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(promptRenderService, templateUsageService, generatedPromptHistoryService);
    }

    @Test
    void generate_guestCannotRenderPublicDraftTemplate() {
        UUID templateId = UUID.randomUUID();
        stubTemplate(templateId, UUID.randomUUID(), true, "draft");

        GeneratePromptRequest request = new GeneratePromptRequest();
        request.setInputValues(Map.of());

        assertThatThrownBy(() -> promptGenerateService.generate(templateId, request, null))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(promptRenderService, templateUsageService, generatedPromptHistoryService);
    }

    @Test
    void generate_authenticatedUserCannotRenderPrivateTemplateFromAnotherWorkspace() {
        UUID templateId = UUID.randomUUID();
        stubTemplate(templateId, UUID.randomUUID(), false, "draft");
        AuthenticatedUser user = new AuthenticatedUser(
                UUID.randomUUID(),
                "user@example.com",
                UUID.randomUUID(),
                false
        );

        GeneratePromptRequest request = new GeneratePromptRequest();
        request.setInputValues(Map.of());

        assertThatThrownBy(() -> promptGenerateService.generate(templateId, request, user))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(promptRenderService, templateUsageService, generatedPromptHistoryService);
    }

    private void stubTemplate(
            UUID templateId,
            UUID workspaceId,
            boolean publicTemplate,
            String status
    ) {
        Template template = new Template();
        template.setId(templateId);
        template.setWorkspaceId(workspaceId);
        template.setPublic(publicTemplate);
        template.setStatus(status);
        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
    }
}
