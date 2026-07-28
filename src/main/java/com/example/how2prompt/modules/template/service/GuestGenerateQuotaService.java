package com.example.how2prompt.modules.template.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Extension point for guest generate limits.
 *
 * <p>The current policy is unlimited. A future implementation can atomically
 * count by client address in Redis and throw a rate-limited exception before
 * prompt rendering starts.</p>
 */
@Service
public class GuestGenerateQuotaService {

    public void checkAndConsume(UUID templateId, String clientAddress) {
        // Unlimited for now; this hook intentionally runs before rendering.
    }
}
