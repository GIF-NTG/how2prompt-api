package com.example.how2prompt.common.security;

import java.util.UUID;

public record AuthenticatedUser(
        UUID userId,
        String email,
        UUID workspaceId,
        boolean admin
) {
}
