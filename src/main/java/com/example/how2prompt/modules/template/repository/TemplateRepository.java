package com.example.how2prompt.modules.template.repository;

import com.example.how2prompt.modules.template.entity.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TemplateRepository extends JpaRepository<Template, UUID>, JpaSpecificationExecutor<Template> {

    Optional<Template> findByWorkspaceIdAndSlug(UUID workspaceId, String slug);

    boolean existsByWorkspaceIdAndSlug(UUID workspaceId, String slug);

    @Query(value = """
            SELECT t.* FROM templates t
            WHERE t.deleted_at IS NULL
              AND (:status IS NULL OR t.status = :status)
              AND (:isPublic IS NULL OR t.is_public = :isPublic)
              AND t.search_vector @@ plainto_tsquery('simple', :search)
              AND (CAST(:categoryId AS uuid) IS NULL OR EXISTS (
                    SELECT 1 FROM template_categories tc
                    WHERE tc.template_id = t.id
                      AND tc.category_id = CAST(:categoryId AS uuid)
              ))
              AND (:hasTags = false OR EXISTS (
                    SELECT 1 FROM template_tags tt
                    WHERE tt.template_id = t.id
                      AND tt.tag_id IN (:tagIds)
              ))
              AND (CAST(:aiModelId AS uuid) IS NULL OR EXISTS (
                    SELECT 1 FROM template_models tm
                    WHERE tm.template_id = t.id
                      AND tm.ai_model_id = CAST(:aiModelId AS uuid)
              ))
              AND (
                    CAST(:cursorCreatedAt AS timestamp) IS NULL
                    OR t.created_at < CAST(:cursorCreatedAt AS timestamp)
                    OR (
                        t.created_at = CAST(:cursorCreatedAt AS timestamp)
                        AND t.id < CAST(:cursorId AS uuid)
                    )
              )
            ORDER BY t.created_at DESC, t.id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Template> searchNewest(
            @Param("search") String search,
            @Param("status") String status,
            @Param("isPublic") Boolean isPublic,
            @Param("categoryId") UUID categoryId,
            @Param("hasTags") boolean hasTags,
            @Param("tagIds") List<UUID> tagIds,
            @Param("aiModelId") UUID aiModelId,
            @Param("cursorCreatedAt") Instant cursorCreatedAt,
            @Param("cursorId") UUID cursorId,
            @Param("limit") int limit
    );

    @Query(value = """
            SELECT t.* FROM templates t
            WHERE t.deleted_at IS NULL
              AND (:status IS NULL OR t.status = :status)
              AND (:isPublic IS NULL OR t.is_public = :isPublic)
              AND t.featured_at IS NOT NULL
              AND t.search_vector @@ plainto_tsquery('simple', :search)
              AND (CAST(:categoryId AS uuid) IS NULL OR EXISTS (
                    SELECT 1 FROM template_categories tc
                    WHERE tc.template_id = t.id
                      AND tc.category_id = CAST(:categoryId AS uuid)
              ))
              AND (:hasTags = false OR EXISTS (
                    SELECT 1 FROM template_tags tt
                    WHERE tt.template_id = t.id
                      AND tt.tag_id IN (:tagIds)
              ))
              AND (CAST(:aiModelId AS uuid) IS NULL OR EXISTS (
                    SELECT 1 FROM template_models tm
                    WHERE tm.template_id = t.id
                      AND tm.ai_model_id = CAST(:aiModelId AS uuid)
              ))
              AND (
                    CAST(:cursorFeaturedAt AS timestamp) IS NULL
                    OR t.featured_at < CAST(:cursorFeaturedAt AS timestamp)
                    OR (
                        t.featured_at = CAST(:cursorFeaturedAt AS timestamp)
                        AND t.id < CAST(:cursorId AS uuid)
                    )
              )
            ORDER BY t.featured_at DESC NULLS LAST, t.id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Template> searchFeatured(
            @Param("search") String search,
            @Param("status") String status,
            @Param("isPublic") Boolean isPublic,
            @Param("categoryId") UUID categoryId,
            @Param("hasTags") boolean hasTags,
            @Param("tagIds") List<UUID> tagIds,
            @Param("aiModelId") UUID aiModelId,
            @Param("cursorFeaturedAt") Instant cursorFeaturedAt,
            @Param("cursorId") UUID cursorId,
            @Param("limit") int limit
    );

    @Query(value = """
            SELECT t.* FROM templates t
            WHERE t.deleted_at IS NULL
              AND (:status IS NULL OR t.status = :status)
              AND (:isPublic IS NULL OR t.is_public = :isPublic)
              AND t.search_vector @@ plainto_tsquery('simple', :search)
              AND (CAST(:categoryId AS uuid) IS NULL OR EXISTS (
                    SELECT 1 FROM template_categories tc
                    WHERE tc.template_id = t.id
                      AND tc.category_id = CAST(:categoryId AS uuid)
              ))
              AND (:hasTags = false OR EXISTS (
                    SELECT 1 FROM template_tags tt
                    WHERE tt.template_id = t.id
                      AND tt.tag_id IN (:tagIds)
              ))
              AND (CAST(:aiModelId AS uuid) IS NULL OR EXISTS (
                    SELECT 1 FROM template_models tm
                    WHERE tm.template_id = t.id
                      AND tm.ai_model_id = CAST(:aiModelId AS uuid)
              ))
              AND (
                    CAST(:cursorUsageCount AS bigint) IS NULL
                    OR t.usage_count < CAST(:cursorUsageCount AS bigint)
                    OR (
                        t.usage_count = CAST(:cursorUsageCount AS bigint)
                        AND t.id < CAST(:cursorId AS uuid)
                    )
              )
            ORDER BY t.usage_count DESC, t.id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Template> searchTrending(
            @Param("search") String search,
            @Param("status") String status,
            @Param("isPublic") Boolean isPublic,
            @Param("categoryId") UUID categoryId,
            @Param("hasTags") boolean hasTags,
            @Param("tagIds") List<UUID> tagIds,
            @Param("aiModelId") UUID aiModelId,
            @Param("cursorUsageCount") Long cursorUsageCount,
            @Param("cursorId") UUID cursorId,
            @Param("limit") int limit
    );

    /**
     * Atomic increment — tránh lost update so với load-rồi-save.
     *
     * @return số row bị ảnh hưởng (0 nếu template không tồn tại / soft-deleted)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Template t SET t.usageCount = t.usageCount + 1 WHERE t.id = :id")
    int incrementUsageCount(@Param("id") UUID id);
}
