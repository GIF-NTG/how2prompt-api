package com.example.how2prompt.modules.identity;

import com.example.how2prompt.common.exception.UnauthorizedException;
import com.example.how2prompt.config.MailProperties;
import com.example.how2prompt.modules.identity.service.EmailVerificationTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationTokenServiceTest {

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private MailProperties mailProperties;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private EmailVerificationTokenService emailVerificationTokenService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        lenient().when(redis.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testCreateToken_NewUser() {
        when(valueOperations.get("email_verify:user:" + userId)).thenReturn(null);
        when(mailProperties.getVerificationTokenTtl()).thenReturn(Duration.ofMinutes(15));

        String rawToken = emailVerificationTokenService.createToken(userId);
        assertNotNull(rawToken);

        verify(valueOperations, times(2)).set(anyString(), anyString(), eq(Duration.ofMinutes(15)));
    }

    @Test
    void testCreateToken_ExistingTokenRevoked() {
        when(valueOperations.get("email_verify:user:" + userId)).thenReturn("oldHash");
        when(mailProperties.getVerificationTokenTtl()).thenReturn(Duration.ofMinutes(15));

        String rawToken = emailVerificationTokenService.createToken(userId);
        assertNotNull(rawToken);

        verify(redis).delete("email_verify:token:oldHash");
        verify(redis).delete("email_verify:user:" + userId);
    }

    @Test
    void testConsumeToken_EmptyToken() {
        assertThrows(UnauthorizedException.class, () -> emailVerificationTokenService.consumeToken(null));
        assertThrows(UnauthorizedException.class, () -> emailVerificationTokenService.consumeToken(""));
    }

    @Test
    void testConsumeToken_TokenNotFound() {
        when(valueOperations.get(anyString())).thenReturn(null);
        assertThrows(UnauthorizedException.class, () -> emailVerificationTokenService.consumeToken("rawToken"));
    }

    @Test
    void testConsumeToken_InvalidUUID() {
        when(valueOperations.get(anyString())).thenReturn("invalid-uuid");
        
        assertThrows(UnauthorizedException.class, () -> emailVerificationTokenService.consumeToken("rawToken"));
        
        verify(redis).delete(anyString());
    }

    @Test
    void testConsumeToken_Success() {
        when(valueOperations.get(anyString())).thenReturn(userId.toString());
        
        UUID result = emailVerificationTokenService.consumeToken("rawToken");
        
        assertEquals(userId, result);
        verify(redis, times(2)).delete(anyString());
    }

    @Test
    void testIsResendOnCooldown() {
        when(redis.hasKey("email_verify:cooldown:test@example.com")).thenReturn(true);
        assertTrue(emailVerificationTokenService.isResendOnCooldown("test@example.com"));

        when(redis.hasKey("email_verify:cooldown:test@example.com")).thenReturn(false);
        assertFalse(emailVerificationTokenService.isResendOnCooldown("test@example.com"));
        
        when(redis.hasKey("email_verify:cooldown:test@example.com")).thenReturn(null);
        assertFalse(emailVerificationTokenService.isResendOnCooldown("test@example.com"));
    }

    @Test
    void testMarkResendCooldown_ValidDuration() {
        when(mailProperties.getResendCooldown()).thenReturn(Duration.ofMinutes(1));
        
        emailVerificationTokenService.markResendCooldown("test@example.com");
        
        verify(valueOperations).set("email_verify:cooldown:test@example.com", "1", Duration.ofMinutes(1));
    }

    @Test
    void testMarkResendCooldown_NullDuration() {
        when(mailProperties.getResendCooldown()).thenReturn(null);
        emailVerificationTokenService.markResendCooldown("test@example.com");
        verify(valueOperations, never()).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void testMarkResendCooldown_ZeroDuration() {
        when(mailProperties.getResendCooldown()).thenReturn(Duration.ZERO);
        emailVerificationTokenService.markResendCooldown("test@example.com");
        verify(valueOperations, never()).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void testMarkResendCooldown_NegativeDuration() {
        when(mailProperties.getResendCooldown()).thenReturn(Duration.ofMinutes(-1));
        emailVerificationTokenService.markResendCooldown("test@example.com");
        verify(valueOperations, never()).set(anyString(), anyString(), any(Duration.class));
    }
}
