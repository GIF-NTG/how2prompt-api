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
 * Composite PK của {@code template_models} (template_id, ai_model_id).
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TemplateModelId implements Serializable {

    @Column(name = "template_id", nullable = false)
    private UUID templateId;

    @Column(name = "ai_model_id", nullable = false)
    private UUID aiModelId;
}
