package com.example.how2prompt.modules.template.repository;

import com.example.how2prompt.modules.template.entity.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TemplateRepository extends JpaRepository<Template, UUID> {

    Optional<Template> findByWorkspaceIdAndSlug(UUID workspaceId, String slug);

    boolean existsByWorkspaceIdAndSlug(UUID workspaceId, String slug);

    /**
     * Atomic increment — tránh lost update so với load-rồi-save.
     *
     * @return số row bị ảnh hưởng (0 nếu template không tồn tại / soft-deleted)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Template t SET t.usageCount = t.usageCount + 1 WHERE t.id = :id")
    int incrementUsageCount(@Param("id") UUID id);
}
