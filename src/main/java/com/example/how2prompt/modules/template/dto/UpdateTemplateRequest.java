package com.example.how2prompt.modules.template.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Partial update — field {@code null} = không đổi.
 * <p>
 * {@code categoryIds}/{@code tagIds}/{@code modelIds} khi không null sẽ
 * <strong>thay thế toàn bộ</strong> quan hệ join hiện tại.
 */
@Getter
@Setter
public class UpdateTemplateRequest {

    private Map<String, Object> titleI18n;

    private Map<String, Object> descriptionI18n;

    @Size(max = 500)
    private String coverImage;

    private Boolean isPublic;

    private Boolean official;

    /**
     * {@code published} → status=published + set published_at;
     * {@code draft} → status=draft + clear published_at.
     * Giá trị khác → 400.
     */
    private String status;

    private List<UUID> categoryIds;

    private List<UUID> tagIds;

    private List<UUID> modelIds;

    /** Phải thuộc {@code modelIds} nếu cả hai đều được gửi. */
    private UUID primaryModelId;
}
