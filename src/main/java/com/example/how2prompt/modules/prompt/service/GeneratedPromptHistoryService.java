package com.example.how2prompt.modules.prompt.service;

import com.example.how2prompt.modules.template.dto.RenderResult;
import com.example.how2prompt.modules.prompt.entity.GeneratedPrompt;
import com.example.how2prompt.modules.prompt.repository.GeneratedPromptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Lưu lịch sử generate bất đồng bộ (US-3.8) — không block UX generate.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeneratedPromptHistoryService {

    private final GeneratedPromptRepository generatedPromptRepository;

    /**
     * Persist {@code generated_prompts} trên thread pool async.
     * Lỗi chỉ log — không lan ra request generate.
     */
    @Async
    @Transactional
    public void saveAsync(
            UUID userId,
            UUID workspaceId,
            RenderResult render,
            Map<String, Object> rawInputValues,
            String title
    ) {
        try {
            GeneratedPrompt row = new GeneratedPrompt();
            row.setUserId(userId);
            row.setWorkspaceId(workspaceId);
            row.setTemplateId(render.templateId());
            row.setTemplateVersionId(render.templateVersionId());
            row.setAiModelId(render.aiModelId());
            row.setTitle(StringUtils.hasText(title) ? title.trim() : null);
            row.setInputValues(rawInputValues != null ? new HashMap<>(rawInputValues) : new HashMap<>());
            row.setExtraInstructions(render.extraInstructions());
            row.setFinalPrompt(render.renderedPrompt());
            generatedPromptRepository.save(row);
            log.debug(
                    "Saved generated_prompt templateId={} userId={} versionId={}",
                    render.templateId(),
                    userId,
                    render.templateVersionId()
            );
        } catch (Exception ex) {
            log.error(
                    "Failed to save generated_prompt history templateId={} userId={}: {}",
                    render.templateId(),
                    userId,
                    ex.getMessage(),
                    ex
            );
        }
    }
}
