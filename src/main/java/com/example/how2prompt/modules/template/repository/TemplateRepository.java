package com.example.how2prompt.modules.template.repository;

import com.example.how2prompt.modules.template.entity.Template;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TemplateRepository extends JpaRepository<Template, UUID> {

    Optional<Template> findByWorkspaceIdAndSlug(UUID workspaceId, String slug);

    boolean existsByWorkspaceIdAndSlug(UUID workspaceId, String slug);
}
