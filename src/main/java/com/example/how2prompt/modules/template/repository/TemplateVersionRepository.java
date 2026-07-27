package com.example.how2prompt.modules.template.repository;

import com.example.how2prompt.modules.template.entity.TemplateVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TemplateVersionRepository extends JpaRepository<TemplateVersion, UUID> {

    List<TemplateVersion> findByTemplateIdOrderByVersionNumberDesc(UUID templateId);

    Optional<TemplateVersion> findByTemplateIdAndCurrentTrue(UUID templateId);

    Optional<TemplateVersion> findByTemplateIdAndVersionNumber(UUID templateId, int versionNumber);
}
