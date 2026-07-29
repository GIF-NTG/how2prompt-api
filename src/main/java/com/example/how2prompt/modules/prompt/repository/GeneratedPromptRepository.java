package com.example.how2prompt.modules.prompt.repository;

import com.example.how2prompt.modules.prompt.entity.GeneratedPrompt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GeneratedPromptRepository extends JpaRepository<GeneratedPrompt, UUID> {

    Page<GeneratedPrompt> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<GeneratedPrompt> findByWorkspaceIdOrderByCreatedAtDesc(UUID workspaceId, Pageable pageable);
}
