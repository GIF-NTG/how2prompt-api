package com.example.how2prompt.modules.prompt.service;

import com.example.how2prompt.modules.template.dto.RenderResult;
import com.example.how2prompt.modules.prompt.entity.GeneratedPrompt;
import com.example.how2prompt.modules.prompt.repository.GeneratedPromptRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class GeneratedPromptHistoryServiceTest {

    private final GeneratedPromptRepository generatedPromptRepository =
            mock(GeneratedPromptRepository.class);
    private final GeneratedPromptHistoryService generatedPromptHistoryService =
            new GeneratedPromptHistoryService(generatedPromptRepository);

    @Test
    void saveAsyncMapsAuthenticatedGenerateHistory() {
        UUID userId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        UUID templateId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        Map<String, Object> inputValues = Map.of("topic", "AI");
        RenderResult render = new RenderResult(
                templateId,
                versionId,
                null,
                "Rendered prompt",
                null,
                false,
                inputValues,
                "Be concise"
        );

        generatedPromptHistoryService.saveAsync(
                userId,
                workspaceId,
                render,
                inputValues,
                "  Saved title  "
        );

        ArgumentCaptor<GeneratedPrompt> captor = ArgumentCaptor.forClass(GeneratedPrompt.class);
        verify(generatedPromptRepository).save(captor.capture());
        GeneratedPrompt saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getWorkspaceId()).isEqualTo(workspaceId);
        assertThat(saved.getTemplateId()).isEqualTo(templateId);
        assertThat(saved.getTemplateVersionId()).isEqualTo(versionId);
        assertThat(saved.getInputValues()).isEqualTo(inputValues);
        assertThat(saved.getExtraInstructions()).isEqualTo("Be concise");
        assertThat(saved.getFinalPrompt()).isEqualTo("Rendered prompt");
        assertThat(saved.getTitle()).isEqualTo("Saved title");
    }
}
