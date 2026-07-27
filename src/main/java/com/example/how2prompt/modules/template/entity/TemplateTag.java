package com.example.how2prompt.modules.template.entity;

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
 * Bảng join {@code template_tags} (V5__templates_core.sql).
 * {@code tag_id} trỏ module taxonomy — chỉ lưu qua composite key, không map entity.
 */
@Entity
@Table(name = "template_tags")
@Getter
@Setter
public class TemplateTag {

    @EmbeddedId
    private TemplateTagId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("templateId")
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;
}
