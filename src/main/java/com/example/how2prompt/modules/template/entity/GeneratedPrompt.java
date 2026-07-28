package com.example.how2prompt.modules.template.entity;

import com.example.how2prompt.common.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Bảng {@code generated_prompts} (V6__generated_prompts.sql) — lịch sử generate (US-3.8).
 * Các cột Phase 2 (ai_score, share_slug, …) chưa map.
 */
@Entity
@Table(name = "generated_prompts")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class GeneratedPrompt extends SoftDeletableEntity {

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "workspace_id", nullable = false, updatable = false)
    private UUID workspaceId;

    @Column(name = "template_id")
    private UUID templateId;

    @Column(name = "template_version_id")
    private UUID templateVersionId;

    @Column(name = "ai_model_id")
    private UUID aiModelId;

    @Column(name = "title", length = 200)
    private String title;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "input_values", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> inputValues = new HashMap<>();

    @Column(name = "extra_instructions", columnDefinition = "TEXT")
    private String extraInstructions;

    @Column(name = "final_prompt", nullable = false, columnDefinition = "TEXT")
    private String finalPrompt;
}
