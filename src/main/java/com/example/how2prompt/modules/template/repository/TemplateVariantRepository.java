package com.example.how2prompt.modules.template.repository;

import com.example.how2prompt.modules.template.entity.TemplateVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TemplateVariantRepository extends JpaRepository<TemplateVariant, UUID> {

    List<TemplateVariant> findByTemplateVersionId(UUID templateVersionId);

    Optional<TemplateVariant> findByTemplateVersionIdAndAiModelId(UUID templateVersionId, UUID aiModelId);
}
