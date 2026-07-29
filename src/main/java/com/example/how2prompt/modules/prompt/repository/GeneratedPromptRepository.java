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
}
