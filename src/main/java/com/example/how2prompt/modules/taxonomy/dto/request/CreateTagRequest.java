package com.example.how2prompt.modules.taxonomy.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTagRequest {

    @NotBlank
    @Size(max = 60)
    private String slug;

    @NotBlank
    @Size(max = 80)
    private String name;
}
