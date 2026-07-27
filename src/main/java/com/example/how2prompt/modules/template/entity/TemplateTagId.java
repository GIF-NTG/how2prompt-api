package com.example.how2prompt.modules.template.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

/**
 * Composite PK của {@code template_tags} (template_id, tag_id).
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TemplateTagId implements Serializable {

    @Column(name = "template_id", nullable = false)
    private UUID templateId;

    @Column(name = "tag_id", nullable = false)
    private UUID tagId;
}
