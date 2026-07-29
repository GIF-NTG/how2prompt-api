package com.example.how2prompt.modules.prompt.exception;

import com.example.how2prompt.common.exception.ResourceNotFoundException;

import java.util.UUID;

/**
 * 404 — generated prompt không tồn tại hoặc đã soft-deleted.
 */
public class GeneratedPromptNotFoundException extends ResourceNotFoundException {

    public GeneratedPromptNotFoundException(UUID id) {
        super("GeneratedPrompt", id);
    }

    public static GeneratedPromptNotFoundException of(UUID id) {
        return new GeneratedPromptNotFoundException(id);
    }
}
