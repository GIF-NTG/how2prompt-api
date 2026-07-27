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
 * Bảng {@code template_versions} (V5__templates_core.sql).
 * <p>
 * Unique partial index {@code idx_template_versions_one_current} đảm bảo chỉ một
 * version {@code is_current = true} per template — mọi thao tác đổi current phải
 * nằm trong {@code @Transactional} và set version cũ về false trước.
 */
@Entity
@Table(
        name = "template_versions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"template_id", "version_number"})
)
@Getter
@Setter
public class TemplateVersion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

    @Column(name = "version_number", nullable = false)
    private int versionNumber;

    @Column(name = "prompt_body", nullable = false, columnDefinition = "TEXT")
    private String promptBody;

    @Column(name = "system_prompt", columnDefinition = "TEXT")
    private String systemPrompt;

    @Column(name = "example_output", columnDefinition = "TEXT")
    private String exampleOutput;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "guide_i18n", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> guideI18n = new HashMap<>();

    @Column(name = "is_current", nullable = false)
    private boolean current = false;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
