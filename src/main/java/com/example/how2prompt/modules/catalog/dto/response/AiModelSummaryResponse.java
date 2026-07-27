package com.example.how2prompt.modules.catalog.dto.response;

import java.util.UUID;

public class AiModelSummaryResponse {
    private UUID id;
    private String code;
    private String name;
    private String iconUrl;

    public AiModelSummaryResponse() {
    }

    public AiModelSummaryResponse(UUID id, String code, String name, String iconUrl) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.iconUrl = iconUrl;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
}
