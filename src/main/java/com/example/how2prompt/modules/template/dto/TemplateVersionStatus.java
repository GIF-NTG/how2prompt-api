package com.example.how2prompt.modules.template.dto;

public record TemplateVersionStatus(
        boolean templateDeleted,
        boolean newerVersionAvailable,
        String latestVersionNumber
) {
}
