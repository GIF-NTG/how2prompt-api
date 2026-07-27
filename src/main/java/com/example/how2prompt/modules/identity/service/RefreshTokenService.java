package com.example.how2prompt.modules.identity.service;

import com.example.how2prompt.common.exception.ErrorCode;
import com.example.how2prompt.common.exception.UnauthorizedException;
import com.example.how2prompt.config.JwtProperties;
import com.example.how2prompt.modules.identity.entity.RefreshToken;
import com.example.how2prompt.modules.identity.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int RAW_TOKEN_BYTES = 32;

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    @Transactional
    public String createRefreshToken(UUID userId) {
        String rawToken = generateRawToken();
        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setExpiresAt(Instant.now().plus(jwtProperties.getRefreshTokenTtl()));

        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    @Transactional
    public RefreshToken verifyAndGetRefreshToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new UnauthorizedException(ErrorCode.TOKEN_INVALID, "Refresh token không hợp lệ.");
        }

        String tokenHash = hashToken(rawToken);
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.TOKEN_INVALID, "Refresh token không hợp lệ."));

        if (refreshToken.getRevokedAt() != null) {
            throw new UnauthorizedException(ErrorCode.TOKEN_INVALID, "Refresh token đã bị thu hồi.");
        }

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException(ErrorCode.TOKEN_EXPIRED, "Refresh token đã hết hạn.");
        }

        return refreshToken;
    }

    /**
     * Rotate token đã verify: revoke cũ, cấp token mới (chống replay).
     *
     * @return raw refresh token mới
     */
    @Transactional
    public String rotateVerifiedToken(RefreshToken existing) {
        existing.setRevokedAt(Instant.now());
        refreshTokenRepository.save(existing);
        return createRefreshToken(existing.getUserId());
    }

    @Transactional
    public void revokeRefreshToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        try {
            String tokenHash = hashToken(rawToken);
            refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
                if (token.getRevokedAt() == null) {
                    token.setRevokedAt(Instant.now());
                    refreshTokenRepository.save(token);
                }
            });
        } catch (Exception ignored) {
            // Invalid format / already gone — logout vẫn clear cookie phía controller
        }
    }

    private static String generateRawToken() {
        byte[] bytes = new byte[RAW_TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
