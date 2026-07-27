package com.example.how2prompt.modules.template.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Bảng join {@code template_models} (V5__templates_core.sql).
 * Gắn template với AI model đích; {@code ai_model_id} trỏ module catalog.
 */
@Entity
@Table(name = "template_models")
@Getter
@Setter
public class TemplateModel {

    @EmbeddedId
    private TemplateModelId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("templateId")
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

    @Column(name = "is_primary", nullable = false)
    private boolean primary = false;
}
