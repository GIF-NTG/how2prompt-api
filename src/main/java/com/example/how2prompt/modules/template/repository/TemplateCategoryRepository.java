package com.example.how2prompt.modules.template.repository;

import com.example.how2prompt.modules.template.entity.TemplateCategory;
import com.example.how2prompt.modules.template.entity.TemplateCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TemplateCategoryRepository extends JpaRepository<TemplateCategory, TemplateCategoryId> {

    List<TemplateCategory> findByIdTemplateId(UUID templateId);

    List<TemplateCategory> findByIdTemplateIdIn(List<UUID> templateIds);

    void deleteByIdTemplateId(UUID templateId);
}
