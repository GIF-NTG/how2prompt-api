package com.example.how2prompt.modules.template.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class CreateTemplateRequest {

    @NotBlank
    @Size(max = 80)
    private String slug;

    @NotNull
    @NotEmpty
    private Map<String, Object> titleI18n;

    private Map<String, Object> descriptionI18n = new HashMap<>();

    @Size(max = 500)
    private String coverImage;

    /** Prompt body của version 1 (bắt buộc khi tạo template). */
    @NotBlank
    private String promptBody;

    private String systemPrompt;

    private String exampleOutput;

    private Map<String, Object> guideI18n = new HashMap<>();
}
