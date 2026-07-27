package com.example.how2prompt.modules.template.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class CreateVariableRequest {

    @NotBlank
    @Size(max = 60)
    private String varKey;

    @NotNull
    @NotEmpty
    private Map<String, Object> labelI18n;

    private Map<String, Object> descriptionI18n = new HashMap<>();

    private Map<String, Object> placeholderI18n = new HashMap<>();

    private Map<String, Object> helpTextI18n = new HashMap<>();

    /** text, textarea, select, multiselect, number, boolean, slider, … */
    @NotBlank
    @Size(max = 20)
    private String inputType;

    private boolean required = false;

    private String defaultValue;

    private List<Object> options = new ArrayList<>();

    private Map<String, Object> validation = new HashMap<>();

    private int sortOrder = 0;
}
