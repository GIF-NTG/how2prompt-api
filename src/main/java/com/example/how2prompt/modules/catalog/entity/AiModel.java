package com.example.how2prompt.modules.catalog.entity;

import com.example.how2prompt.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Bảng {@code ai_models} (V3__ai_catalog.sql).
 * Entity nội bộ module catalog — module khác chỉ gọi qua {@code AiModelQueryService}.
 */
@Entity
@Table(name = "ai_models")
@Getter
@Setter
public class AiModel extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 60)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "provider", nullable = false, length = 40)
    private String provider;

    @Column(name = "model_type", nullable = false, length = 20)
    private String modelType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "capabilities", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> capabilities = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "default_config", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> defaultConfig = new HashMap<>();

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Column(name = "doc_url", length = 500)
    private String docUrl;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;
}
