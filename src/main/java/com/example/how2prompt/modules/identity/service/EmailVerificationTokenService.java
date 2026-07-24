package com.example.how2prompt.modules.identity.service;

import com.example.how2prompt.common.exception.ErrorCode;
import com.example.how2prompt.common.exception.UnauthorizedException;
import com.example.how2prompt.config.MailProperties;
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

/**
 * Email verification token lưu Redis (không dùng bảng DB).
 * <pre>
 *   email_verify:token:{sha256(raw)} -> userId
 *   email_verify:user:{userId}       -> sha256(raw)   (để revoke token cũ khi resend)
 * </pre>
 */
@Service
@RequiredArgsConstructor
public class EmailVerificationTokenService {

    private static final String KEY_TOKEN_PREFIX = "email_verify:token:";
    private static final String KEY_USER_PREFIX = "email_verify:user:";
    private static final String KEY_COOLDOWN_PREFIX = "email_verify:cooldown:";
    private static final int RAW_TOKEN_BYTES = 32;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final StringRedisTemplate redis;
    private final MailProperties mailProperties;

    /**
     * Tạo raw token mới, lưu hash vào Redis (TTL), revoke token cũ của user nếu có.
     *
     * @return raw token gửi trong email (chỉ biết lúc này)
     */
    public String createToken(UUID userId) {
        revokeExistingForUser(userId);

        String rawToken = generateRawToken();
        String tokenHash = hashToken(rawToken);
        Duration ttl = mailProperties.getVerificationTokenTtl();

        redis.opsForValue().set(tokenKey(tokenHash), userId.toString(), ttl);
        redis.opsForValue().set(userKey(userId), tokenHash, ttl);

        return rawToken;
    }

    /**
     * Consume token (one-shot): verify + xóa khỏi Redis.
     *
     * @return userId gắn với token
     */
    public UUID consumeToken(String rawToken) {
        if (!StringUtils.hasText(rawToken)) {
            throw new UnauthorizedException(ErrorCode.TOKEN_INVALID, "Token xác minh không hợp lệ.");
        }

        String tokenHash = hashToken(rawToken.trim());
        String userIdStr = redis.opsForValue().get(tokenKey(tokenHash));
        if (!StringUtils.hasText(userIdStr)) {
            throw new UnauthorizedException(
                    ErrorCode.TOKEN_INVALID,
                    "Token xác minh không hợp lệ hoặc đã hết hạn."
            );
        }

        UUID userId;
        try {
            userId = UUID.fromString(userIdStr);
        } catch (IllegalArgumentException e) {
            redis.delete(tokenKey(tokenHash));
            throw new UnauthorizedException(ErrorCode.TOKEN_INVALID, "Token xác minh không hợp lệ.");
        }

        redis.delete(tokenKey(tokenHash));
        redis.delete(userKey(userId));
        return userId;
    }

    /**
     * Cooldown resend (tránh spam). {@code true} nếu còn trong cooldown.
     */
    public boolean isResendOnCooldown(String email) {
        Boolean has = redis.hasKey(cooldownKey(email));
        return Boolean.TRUE.equals(has);
    }

    public void markResendCooldown(String email) {
        Duration cooldown = mailProperties.getResendCooldown();
        if (cooldown != null && !cooldown.isZero() && !cooldown.isNegative()) {
            redis.opsForValue().set(cooldownKey(email), "1", cooldown);
        }
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
