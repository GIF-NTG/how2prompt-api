package com.example.how2prompt.modules.template.entity;

import com.example.how2prompt.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Bảng {@code template_variants} (V5__templates_core.sql).
 * Variant theo {@code ai_model_id}: khi generate, nếu có variant khớp model thì
 * dùng {@code prompt_body_override} thay thế toàn bộ (không merge).
 */
@Entity
@Table(
        name = "template_variants",
        uniqueConstraints = @UniqueConstraint(columnNames = {"template_version_id", "ai_model_id"})
)
@Getter
@Setter
public class TemplateVariant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_version_id", nullable = false)
    private TemplateVersion templateVersion;

    /** FK tới {@code ai_models.id} (module catalog) — chỉ lưu UUID, không map entity. */
    @Column(name = "ai_model_id", nullable = false)
    private UUID aiModelId;

    @Column(name = "prompt_body_override", columnDefinition = "TEXT")
    private String promptBodyOverride;

    @Column(name = "system_prompt_override", columnDefinition = "TEXT")
    private String systemPromptOverride;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "model_config", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> modelConfig = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "notes_i18n", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> notesI18n = new HashMap<>();

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;
}
