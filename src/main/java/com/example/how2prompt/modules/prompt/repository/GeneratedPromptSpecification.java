package com.example.how2prompt.modules.prompt.repository;

import com.example.how2prompt.common.utils.CursorUtil.DecodedCursor;
import com.example.how2prompt.modules.prompt.entity.GeneratedPrompt;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Xây dựng truy vấn động Specification cho GeneratedPrompt (US-4.2).
 */
public class GeneratedPromptSpecification {

    public static Specification<GeneratedPrompt> getHistorySpec(
            UUID userId,
            UUID templateId,
            UUID aiModelId,
            String search,
            DecodedCursor decodedCursor
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Bắt buộc: user_id
            predicates.add(cb.equal(root.get("userId"), userId));

            // Lọc theo template_id
            if (templateId != null) {
                predicates.add(cb.equal(root.get("templateId"), templateId));
            }

            // Lọc theo ai_model_id
            if (aiModelId != null) {
                predicates.add(cb.equal(root.get("aiModelId"), aiModelId));
            }

            // Lọc theo từ khóa tìm kiếm (search khớp với title hoặc finalPrompt)
            if (StringUtils.hasText(search)) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("finalPrompt")), pattern)
                ));
            }

            // Xử lý cursor phân trang: (createdAt < cursorCreatedAt) HOẶC (createdAt = cursorCreatedAt VÀ id < cursorId)
            if (decodedCursor != null && decodedCursor.getSortValue() instanceof Instant cursorCreatedAt) {
                predicates.add(cb.or(
                        cb.lessThan(root.get("createdAt"), cursorCreatedAt),
                        cb.and(
                                cb.equal(root.get("createdAt"), cursorCreatedAt),
                                cb.lessThan(root.get("id"), decodedCursor.getId())
                        )
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
