package com.example.how2prompt.modules.template.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class GeneratePromptRequest {

    /** Model AI đích (US-3.5). null = không áp variant. */
    private UUID aiModelId;

    /** Giá trị form theo var_key (US-3.1 / US-3.2). */
    @NotNull
    @Size(max = 100)
    private Map<String, Object> inputValues = new HashMap<>();

    /** Free-form instructions cuối form (US-3.4). */
    @Size(max = 10_000)
    private String extraInstructions;

    /** Tiêu đề lưu lịch sử (optional). */
    @Size(max = 200)
    private String title;
}
