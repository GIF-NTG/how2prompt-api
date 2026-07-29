package com.example.how2prompt.modules.prompt.service;

import com.example.how2prompt.common.exception.BadRequestException;
import com.example.how2prompt.common.exception.ForbiddenException;
import com.example.how2prompt.common.response.PageResponse;
import com.example.how2prompt.common.utils.CursorUtil;
import com.example.how2prompt.common.utils.CursorUtil.DecodedCursor;
import com.example.how2prompt.modules.prompt.dto.response.PromptHistoryDetailResponse;
import com.example.how2prompt.modules.prompt.dto.response.PromptHistoryResponse;
import com.example.how2prompt.modules.prompt.entity.GeneratedPrompt;
import com.example.how2prompt.modules.prompt.exception.GeneratedPromptNotFoundException;
import com.example.how2prompt.modules.prompt.repository.GeneratedPromptRepository;
import com.example.how2prompt.modules.prompt.repository.GeneratedPromptSpecification;
import com.example.how2prompt.modules.template.dto.RenderResult;
import com.example.how2prompt.modules.template.dto.TemplateVersionStatus;
import com.example.how2prompt.modules.template.service.TemplateQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Lưu lịch sử generate bất đồng bộ (US-3.8) — không block UX generate.
 * Đồng thời quản lý lịch sử (truy vấn, xóa, xem chi tiết) của user (US-4.2, 4.3, 4.5).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeneratedPromptHistoryService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final GeneratedPromptRepository generatedPromptRepository;
    private final TemplateQueryService templateQueryService;

    /**
     * Lấy danh sách lịch sử có bộ lọc và phân trang cursor (US-4.2).
     */
    @Transactional(readOnly = true)
    public PageResponse<PromptHistoryResponse> getHistory(
            UUID userId,
            UUID templateId,
            UUID aiModelId,
            String search,
            String cursor,
            int limit
    ) {
        if (userId == null) {
            throw new BadRequestException("User ID không được để trống.");
        }

        int resolvedLimit = resolveLimit(limit);
        DecodedCursor decodedCursor = CursorUtil.decode(cursor, "newest");

        Specification<GeneratedPrompt> spec = GeneratedPromptSpecification.getHistorySpec(
                userId,
                templateId,
                aiModelId,
                search,
                decodedCursor
        );

        Sort sort = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
        List<GeneratedPrompt> prompts = generatedPromptRepository.findAll(spec, PageRequest.of(0, resolvedLimit + 1, sort)).getContent();

        boolean hasMore = prompts.size() > resolvedLimit;
        List<GeneratedPrompt> resultList = hasMore ? prompts.subList(0, resolvedLimit) : prompts;

        String nextCursor = null;
        if (!resultList.isEmpty() && hasMore) {
            GeneratedPrompt lastItem = resultList.get(resultList.size() - 1);
            nextCursor = CursorUtil.encode(lastItem.getCreatedAt(), lastItem.getId());
        }

        List<PromptHistoryResponse> dtoList = resultList.stream()
                .map(PromptHistoryResponse::from)
                .collect(Collectors.toList());

        return new PageResponse<>(dtoList, nextCursor, hasMore);
    }

    /**
     * Lấy chi tiết lịch sử để tái điền form (US-4.3).
     * Kiểm tra quyền sở hữu lịch sử, kiểm tra trạng thái version của template gốc qua TemplateQueryService.
     */
    @Transactional(readOnly = true)
    public PromptHistoryDetailResponse getById(UUID id, UUID userId) {
        if (id == null) {
            throw new BadRequestException("ID lịch sử không được để trống.");
        }
        if (userId == null) {
            throw new BadRequestException("User ID không được để trống.");
        }

        GeneratedPrompt prompt = generatedPromptRepository.findById(id)
                .orElseThrow(() -> GeneratedPromptNotFoundException.of(id));

        if (!userId.equals(prompt.getUserId())) {
            throw new ForbiddenException("Bạn không có quyền truy cập lịch sử này.");
        }

        TemplateVersionStatus status = templateQueryService.checkTemplateVersionStatus(
                prompt.getTemplateId(),
                prompt.getTemplateVersionId()
        );

        return new PromptHistoryDetailResponse(
                prompt.getId(),
                prompt.getTemplateId(),
                prompt.getTemplateVersionId(),
                prompt.getAiModelId(),
                prompt.getTitle(),
                prompt.getInputValues(),
                prompt.getExtraInstructions(),
                prompt.getFinalPrompt(),
                prompt.getCreatedAt(),
                status.templateDeleted(),
                status.newerVersionAvailable(),
                status.latestVersionNumber()
        );
    }

    /**
     * Xóa mềm lịch sử (US-4.5).
     */
    @Transactional
    public void delete(UUID id, UUID userId) {
        if (id == null) {
            throw new BadRequestException("ID lịch sử không được để trống.");
        }
        if (userId == null) {
            throw new BadRequestException("User ID không được để trống.");
        }

        GeneratedPrompt prompt = generatedPromptRepository.findById(id)
                .orElseThrow(() -> GeneratedPromptNotFoundException.of(id));

        if (!userId.equals(prompt.getUserId())) {
            throw new ForbiddenException("Bạn không có quyền xóa lịch sử này.");
        }

        prompt.setDeletedAt(Instant.now());
        generatedPromptRepository.save(prompt);
    }

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

    private int resolveLimit(int limit) {
        if (limit < 1 || limit > MAX_LIMIT) {
            return DEFAULT_LIMIT;
        }
        return limit;
    }
}
