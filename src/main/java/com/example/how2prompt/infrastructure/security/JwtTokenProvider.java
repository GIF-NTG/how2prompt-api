package com.example.how2prompt.infrastructure.security;

import com.example.how2prompt.config.JwtProperties;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_WORKSPACE_ID = "workspace_id";
    private static final String CLAIM_ADMIN = "admin";
    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;
    private final JwtProperties jwtProperties;

    public String generateAccessToken(UUID userId, String email, UUID workspaceId, boolean admin) {
        Instant now = Instant.now();

        JwtBuilder builder = Jwts.builder()
                .subject(userId.toString())
                .issuer(jwtProperties.getIssuer())
                .claim(CLAIM_EMAIL, email)
                .claim(CLAIM_ADMIN, admin)
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(jwtProperties.getAccessTokenTtl())));

        if (workspaceId != null) {
            builder.claim(CLAIM_WORKSPACE_ID, workspaceId.toString());
        }

        return builder.signWith(privateKey, Jwts.SIG.RS256).compact();
    }

    public String generateRefreshToken(UUID userId, String jti) {
        Instant now = Instant.now();

        return Jwts.builder()
                .id(jti)
                .subject(userId.toString())
                .issuer(jwtProperties.getIssuer())
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(jwtProperties.getRefreshTokenTtl())))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token);
    }

    public boolean isAccessToken(Claims claims) {
        return TYPE_ACCESS.equals(claims.get(CLAIM_TYPE, String.class));
    }

    public boolean isRefreshToken(Claims claims) {
        return TYPE_REFRESH.equals(claims.get(CLAIM_TYPE, String.class));
    }

    public UUID extractUserId(Claims claims) {
        return UUID.fromString(claims.getSubject());
    }

    public String extractEmail(Claims claims) {
        return claims.get(CLAIM_EMAIL, String.class);
    }

    public UUID extractWorkspaceId(Claims claims) {
        String raw = claims.get(CLAIM_WORKSPACE_ID, String.class);
        return raw != null ? UUID.fromString(raw) : null;
    }

    public boolean extractAdmin(Claims claims) {
        Boolean value = claims.get(CLAIM_ADMIN, Boolean.class);
        return Boolean.TRUE.equals(value);
    }
}
