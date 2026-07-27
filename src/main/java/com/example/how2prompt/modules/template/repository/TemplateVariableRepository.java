package com.example.how2prompt.modules.template.repository;

import com.example.how2prompt.modules.template.entity.TemplateVariable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TemplateVariableRepository extends JpaRepository<TemplateVariable, UUID> {

    List<TemplateVariable> findByTemplateVersionIdOrderBySortOrderAsc(UUID templateVersionId);

    Optional<TemplateVariable> findByTemplateVersionIdAndVarKey(UUID templateVersionId, String varKey);
}
