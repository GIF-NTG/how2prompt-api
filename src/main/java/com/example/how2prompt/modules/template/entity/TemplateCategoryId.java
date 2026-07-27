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
 * Composite PK của {@code template_categories} (template_id, category_id).
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TemplateCategoryId implements Serializable {

    @Column(name = "template_id", nullable = false)
    private UUID templateId;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;
}
