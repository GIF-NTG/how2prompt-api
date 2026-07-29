package com.example.how2prompt.modules.identity.service;

import com.example.how2prompt.common.exception.ErrorCode;
import com.example.how2prompt.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenService {

    private static final String KEY_TOKEN_PREFIX = "password_reset:token:";
    private static final String KEY_USER_PREFIX = "password_reset:user:";
    private static final String KEY_COOLDOWN_PREFIX = "password_reset:cooldown:";
    private static final int RAW_TOKEN_BYTES = 32;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Duration TOKEN_TTL = Duration.ofMinutes(15);
    private static final Duration COOLDOWN = Duration.ofSeconds(60);

    private final StringRedisTemplate redis;

    public String createToken(UUID userId) {
        revokeExistingForUser(userId);

        String rawToken = generateRawToken();
        String tokenHash = hashToken(rawToken);

        redis.opsForValue().set(tokenKey(tokenHash), userId.toString(), TOKEN_TTL);
        redis.opsForValue().set(userKey(userId), tokenHash, TOKEN_TTL);

        return rawToken;
    }

    public UUID consumeToken(String rawToken) {
        if (!StringUtils.hasText(rawToken)) {
            throw new UnauthorizedException(ErrorCode.TOKEN_INVALID, "Token khôi phục mật khẩu không hợp lệ.");
        }

        String tokenHash = hashToken(rawToken.trim());
        String userIdStr = redis.opsForValue().get(tokenKey(tokenHash));
        if (!StringUtils.hasText(userIdStr)) {
            throw new UnauthorizedException(
                    ErrorCode.TOKEN_INVALID,
                    "Token khôi phục mật khẩu không hợp lệ hoặc đã hết hạn."
            );
        }

        UUID userId;
        try {
            userId = UUID.fromString(userIdStr);
        } catch (IllegalArgumentException e) {
            redis.delete(tokenKey(tokenHash));
            throw new UnauthorizedException(ErrorCode.TOKEN_INVALID, "Token khôi phục mật khẩu không hợp lệ.");
        }

        redis.delete(tokenKey(tokenHash));
        redis.delete(userKey(userId));
        return userId;
    }

    public boolean isResendOnCooldown(String email) {
        Boolean has = redis.hasKey(cooldownKey(email));
        return Boolean.TRUE.equals(has);
    }

    public void markResendCooldown(String email) {
        redis.opsForValue().set(cooldownKey(email), "1", COOLDOWN);
    }

    private void revokeExistingForUser(UUID userId) {
        String existingHash = redis.opsForValue().get(userKey(userId));
        if (StringUtils.hasText(existingHash)) {
            redis.delete(tokenKey(existingHash));
            redis.delete(userKey(userId));
        }
    }

    private static String tokenKey(String tokenHash) {
        return KEY_TOKEN_PREFIX + tokenHash;
    }

    private static String userKey(UUID userId) {
        return KEY_USER_PREFIX + userId;
    }

    private static String cooldownKey(String email) {
        return KEY_COOLDOWN_PREFIX + email.toLowerCase();
    }

    private static String generateRawToken() {
        byte[] bytes = new byte[RAW_TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
