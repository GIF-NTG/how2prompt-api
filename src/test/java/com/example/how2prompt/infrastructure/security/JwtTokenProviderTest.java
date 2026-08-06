package com.example.how2prompt.infrastructure.security;

import com.example.how2prompt.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private JwtProperties jwtProperties;

    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        privateKey = (RSAPrivateKey) keyPair.getPrivate();
        publicKey = (RSAPublicKey) keyPair.getPublic();

        jwtTokenProvider = new JwtTokenProvider(privateKey, publicKey, jwtProperties);
    }

    @Test
    void testGenerateAccessToken_WithWorkspace() {
        when(jwtProperties.getIssuer()).thenReturn("how2prompt");
        when(jwtProperties.getAccessTokenTtl()).thenReturn(Duration.ofMinutes(15));

        UUID userId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();
        String token = jwtTokenProvider.generateAccessToken(userId, "test@test.com", workspaceId, true);

        assertNotNull(token);
        
        Jws<Claims> parsed = jwtTokenProvider.parse(token);
        Claims claims = parsed.getPayload();
        
        assertTrue(jwtTokenProvider.isAccessToken(claims));
        assertFalse(jwtTokenProvider.isRefreshToken(claims));
        assertEquals(userId, jwtTokenProvider.extractUserId(claims));
        assertEquals("test@test.com", jwtTokenProvider.extractEmail(claims));
        assertEquals(workspaceId, jwtTokenProvider.extractWorkspaceId(claims));
        assertTrue(jwtTokenProvider.extractAdmin(claims));
    }

    @Test
    void testGenerateAccessToken_WithoutWorkspace() {
        when(jwtProperties.getIssuer()).thenReturn("how2prompt");
        when(jwtProperties.getAccessTokenTtl()).thenReturn(Duration.ofMinutes(15));

        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateAccessToken(userId, "user@test.com", null, false);

        assertNotNull(token);
        
        Jws<Claims> parsed = jwtTokenProvider.parse(token);
        Claims claims = parsed.getPayload();
        
        assertTrue(jwtTokenProvider.isAccessToken(claims));
        assertEquals(userId, jwtTokenProvider.extractUserId(claims));
        assertEquals("user@test.com", jwtTokenProvider.extractEmail(claims));
        assertNull(jwtTokenProvider.extractWorkspaceId(claims));
        assertFalse(jwtTokenProvider.extractAdmin(claims));
    }

    @Test
    void testGenerateRefreshToken() {
        when(jwtProperties.getIssuer()).thenReturn("how2prompt");
        when(jwtProperties.getRefreshTokenTtl()).thenReturn(Duration.ofDays(7));

        UUID userId = UUID.randomUUID();
        String jti = UUID.randomUUID().toString();
        String token = jwtTokenProvider.generateRefreshToken(userId, jti);

        assertNotNull(token);
        
        Jws<Claims> parsed = jwtTokenProvider.parse(token);
        Claims claims = parsed.getPayload();
        
        assertTrue(jwtTokenProvider.isRefreshToken(claims));
        assertFalse(jwtTokenProvider.isAccessToken(claims));
        assertEquals(userId, jwtTokenProvider.extractUserId(claims));
        assertEquals(jti, claims.getId());
    }
}
