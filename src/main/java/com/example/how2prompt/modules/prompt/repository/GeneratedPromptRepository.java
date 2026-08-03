package com.example.how2prompt.modules.prompt.repository;

import com.example.how2prompt.modules.prompt.entity.GeneratedPrompt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface GeneratedPromptRepository extends JpaRepository<GeneratedPrompt, UUID>, JpaSpecificationExecutor<GeneratedPrompt> {

    Page<GeneratedPrompt> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<GeneratedPrompt> findByWorkspaceIdOrderByCreatedAtDesc(UUID workspaceId, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(DISTINCT gp.userId) FROM GeneratedPrompt gp WHERE gp.userId IS NOT NULL")
    long countDistinctUsersWithGenerations();

    @org.springframework.data.jpa.repository.Query(value = """
            SELECT CAST(gp.created_at AS date) as day, COUNT(*) as cnt
            FROM generated_prompts gp
            WHERE gp.created_at >= :since
            GROUP BY CAST(gp.created_at AS date)
            ORDER BY day ASC
            """, nativeQuery = true)
    java.util.List<Object[]> countPromptsPerDaySince(@org.springframework.data.repository.query.Param("since") java.time.Instant since);

    @org.springframework.data.jpa.repository.Query(value = """
            SELECT gp.ai_model_id, m.name, COUNT(*) as cnt
            FROM generated_prompts gp
            JOIN ai_models m ON gp.ai_model_id = m.id
            WHERE gp.ai_model_id IS NOT NULL
            GROUP BY gp.ai_model_id, m.name
            ORDER BY cnt DESC
            LIMIT :limit
            """, nativeQuery = true)
    java.util.List<Object[]> countMostUsedModels(@org.springframework.data.repository.query.Param("limit") int limit);
}
