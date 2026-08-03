package com.example.how2prompt.modules.taxonomy.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TagMergeRequest {

    @NotNull(message = "sourceTagId không được để trống.")
    private UUID sourceTagId;

    @NotNull(message = "targetTagId không được để trống.")
    private UUID targetTagId;
}
