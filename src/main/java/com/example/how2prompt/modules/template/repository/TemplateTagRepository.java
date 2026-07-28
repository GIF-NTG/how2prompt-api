package com.example.how2prompt.modules.template.repository;

import com.example.how2prompt.modules.template.entity.TemplateTag;
import com.example.how2prompt.modules.template.entity.TemplateTagId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TemplateTagRepository extends JpaRepository<TemplateTag, TemplateTagId> {

    List<TemplateTag> findByIdTemplateId(UUID templateId);

    List<TemplateTag> findByIdTemplateIdIn(List<UUID> templateIds);

    void deleteByIdTemplateId(UUID templateId);
}
