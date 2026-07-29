package com.example.how2prompt.modules.template.entity;

import com.example.how2prompt.common.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Bảng {@code templates} (V5__templates_core.sql) — Phase 1 core.
 * <p>
 * Không map {@code search_vector} (tsvector do DB trigger quản lý).
 * Các cột Phase 2/3 (forked_from_*, fork_count, quality_score, …) chưa có trong schema.
 */
@Entity
@Table(
        name = "templates",
        uniqueConstraints = @UniqueConstraint(columnNames = {"workspace_id", "slug"})
)
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class Template extends SoftDeletableEntity {

    @Column(name = "workspace_id", nullable = false, updatable = false)
    private UUID workspaceId;

    @Column(name = "slug", nullable = false, length = 80)
    private String slug;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "title_i18n", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> titleI18n = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "description_i18n", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> descriptionI18n = new HashMap<>();

    @Column(name = "cover_image", length = 500)
    private String coverImage;

    @Column(name = "author_id")
    private UUID authorId;

    @Column(name = "author_type", nullable = false, length = 20)
    private String authorType = "admin";

    @Column(name = "is_official", nullable = false)
    private boolean official = false;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic = false;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "draft";

    /** FK tới {@code template_versions.id}; set sau khi tạo version hiện tại. */
    @Column(name = "current_version_id")
    private UUID currentVersionId;

    @Column(name = "usage_count", nullable = false)
    private long usageCount = 0L;

    @Column(name = "favorite_count", nullable = false)
    private int favoriteCount = 0;

    @Column(name = "featured_at")
    private Instant featuredAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "view_count", nullable = false)
    private long viewCount = 0L;
}
