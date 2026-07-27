package com.example.how2prompt.modules.template.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class CreateVariantRequest {

    @NotNull
    private UUID aiModelId;

    private String promptBodyOverride;

    private String systemPromptOverride;

    private Map<String, Object> modelConfig = new HashMap<>();

    private Map<String, Object> notesI18n = new HashMap<>();
}
