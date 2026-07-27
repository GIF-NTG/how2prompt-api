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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bảng {@code template_variables} (V5__templates_core.sql).
 * Form động (Epic 3) đọc {@code input_type}, {@code options}, {@code validation}.
 */
@Entity
@Table(
        name = "template_variables",
        uniqueConstraints = @UniqueConstraint(columnNames = {"template_version_id", "var_key"})
)
@Getter
@Setter
public class TemplateVariable extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_version_id", nullable = false)
    private TemplateVersion templateVersion;

    @Column(name = "var_key", nullable = false, length = 60)
    private String varKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "label_i18n", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> labelI18n = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "description_i18n", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> descriptionI18n = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "placeholder_i18n", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> placeholderI18n = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "help_text_i18n", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> helpTextI18n = new HashMap<>();

    /** text, textarea, select, multiselect, number, boolean, slider, … */
    @Column(name = "input_type", nullable = false, length = 20)
    private String inputType;

    @Column(name = "is_required", nullable = false)
    private boolean required = false;

    @Column(name = "default_value", columnDefinition = "TEXT")
    private String defaultValue;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options", nullable = false, columnDefinition = "jsonb")
    private List<Object> options = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "validation", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> validation = new HashMap<>();

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
