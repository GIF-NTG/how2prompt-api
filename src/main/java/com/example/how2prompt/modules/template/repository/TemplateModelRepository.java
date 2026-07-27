package com.example.how2prompt.modules.template.repository;

import com.example.how2prompt.modules.template.entity.TemplateModel;
import com.example.how2prompt.modules.template.entity.TemplateModelId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TemplateModelRepository extends JpaRepository<TemplateModel, TemplateModelId> {

    List<TemplateModel> findByIdTemplateId(UUID templateId);

    void deleteByIdTemplateId(UUID templateId);
}
