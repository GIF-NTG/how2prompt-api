package com.example.how2prompt.modules.template.service;

import com.example.how2prompt.modules.template.dto.RenderResult;
import com.example.how2prompt.modules.template.entity.Template;
import com.example.how2prompt.modules.template.entity.TemplateVariable;
import com.example.how2prompt.modules.template.entity.TemplateVariant;
import com.example.how2prompt.modules.template.entity.TemplateVersion;
import com.example.how2prompt.modules.template.exception.TemplateValidationException;
import com.example.how2prompt.modules.template.repository.TemplateRepository;
import com.example.how2prompt.modules.template.repository.TemplateVariableRepository;
import com.example.how2prompt.modules.template.repository.TemplateVariantRepository;
import com.example.how2prompt.modules.template.repository.TemplateVersionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromptRenderServiceTest {

    @Mock
    private TemplateRepository templateRepository;
    @Mock
    private TemplateVersionRepository templateVersionRepository;
    @Mock
    private TemplateVariableRepository templateVariableRepository;
    @Mock
    private TemplateVariantRepository templateVariantRepository;

    private PromptRenderService promptRenderService;

    private final UUID templateId = UUID.randomUUID();
    private final UUID versionId = UUID.randomUUID();
    private final UUID modelId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        promptRenderService = new PromptRenderService(
                templateRepository,
                templateVersionRepository,
                templateVariableRepository,
                templateVariantRepository,
                new TemplateVariableValidator()
        );
    }

    @Test
    void render_substitutesPlaceholders_usesDefault_andAppendsExtra() {
        stubTemplateAndVersion("Write about {{topic}} in {{tone}} style. Tags: {{tags}}");

        TemplateVariable topic = variable("topic", "text", true, null);
        TemplateVariable tone = variable("tone", "text", false, "formal");
        TemplateVariable tags = variable("tags", "multiselect", false, null);
        when(templateVariableRepository.findByTemplateVersionIdOrderBySortOrderAsc(versionId))
                .thenReturn(List.of(topic, tone, tags));
        when(templateVariantRepository.findByTemplateVersionIdAndAiModelId(versionId, modelId))
                .thenReturn(Optional.empty());

        RenderResult result = promptRenderService.render(
                templateId,
                modelId,
                Map.of("topic", "AI prompts", "tags", List.of("seo", "copy")),
                "Keep it short."
        );

        assertThat(result.renderedPrompt()).isEqualTo(
                "Write about AI prompts in formal style. Tags: seo, copy\n\nKeep it short."
        );
        assertThat(result.usedVariant()).isFalse();
        assertThat(result.templateVersionId()).isEqualTo(versionId);
        assertThat(result.resolvedInputValues()).containsEntry("tone", "formal");
        assertThat(result.extraInstructions()).isEqualTo("Keep it short.");
    }

    @Test
    void render_usesVariantOverride_notMerge() {
        stubTemplateAndVersion("BASE {{name}}");

        TemplateVariable name = variable("name", "text", true, null);
        when(templateVariableRepository.findByTemplateVersionIdOrderBySortOrderAsc(versionId))
                .thenReturn(List.of(name));

        TemplateVariant variant = new TemplateVariant();
        variant.setAiModelId(modelId);
        variant.setPromptBodyOverride("OVERRIDE only: {{name}}");
        variant.setSystemPromptOverride("System override");
        when(templateVariantRepository.findByTemplateVersionIdAndAiModelId(versionId, modelId))
                .thenReturn(Optional.of(variant));

        RenderResult result = promptRenderService.render(
                templateId,
                modelId,
                Map.of("name", "Ada"),
                null
        );

        assertThat(result.renderedPrompt()).isEqualTo("OVERRIDE only: Ada");
        assertThat(result.systemPrompt()).isEqualTo("System override");
        assertThat(result.usedVariant()).isTrue();
    }

    @Test
    void render_unknownPlaceholder_keepsOriginal() {
        stubTemplateAndVersion("Hello {{name}} and {{typo_key}}");

        TemplateVariable name = variable("name", "text", true, null);
        when(templateVariableRepository.findByTemplateVersionIdOrderBySortOrderAsc(versionId))
                .thenReturn(List.of(name));
        when(templateVariantRepository.findByTemplateVersionIdAndAiModelId(versionId, modelId))
                .thenReturn(Optional.empty());

        RenderResult result = promptRenderService.render(
                templateId,
                modelId,
                Map.of("name", "World"),
                null
        );

        assertThat(result.renderedPrompt()).isEqualTo("Hello World and {{typo_key}}");
    }

    @Test
    void render_validationFailure_throwsTemplateValidationException() {
        stubTemplateAndVersion("Hi {{name}}");

        TemplateVariable name = variable("name", "text", true, null);
        when(templateVariableRepository.findByTemplateVersionIdOrderBySortOrderAsc(versionId))
                .thenReturn(List.of(name));

        assertThatThrownBy(() -> promptRenderService.render(templateId, modelId, Map.of(), null))
                .isInstanceOf(TemplateValidationException.class)
                .satisfies(ex -> {
                    TemplateValidationException tve = (TemplateValidationException) ex;
                    assertThat(tve.getCode()).isEqualTo("VALIDATION_ERROR");
                    assertThat(tve.getFieldErrors()).isNotEmpty();
                    assertThat(tve.getDetails()).containsKey("fields");
                });
    }

    private void stubTemplateAndVersion(String promptBody) {
        Template template = new Template();
        template.setId(templateId);
        template.setCurrentVersionId(versionId);
        template.setSlug("demo");
        template.setTitleI18n(Map.of("en", "Demo"));
        template.setWorkspaceId(UUID.randomUUID());

        TemplateVersion version = new TemplateVersion();
        version.setId(versionId);
        version.setPromptBody(promptBody);
        version.setSystemPrompt("Be helpful");
        version.setCurrent(true);
        version.setTemplate(template);

        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
        when(templateVersionRepository.findById(versionId)).thenReturn(Optional.of(version));
    }

    private static TemplateVariable variable(
            String key,
            String inputType,
            boolean required,
            String defaultValue
    ) {
        TemplateVariable v = new TemplateVariable();
        v.setVarKey(key);
        v.setInputType(inputType);
        v.setRequired(required);
        v.setDefaultValue(defaultValue);
        v.setLabelI18n(Map.of("en", key));
        return v;
    }
}
