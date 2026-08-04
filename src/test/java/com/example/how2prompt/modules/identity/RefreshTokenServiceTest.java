package com.example.how2prompt.modules.identity;

import com.example.how2prompt.modules.identity.entity.RefreshToken;
import com.example.how2prompt.modules.identity.entity.User;
import com.example.how2prompt.modules.identity.repository.RefreshTokenRepository;
import com.example.how2prompt.modules.identity.repository.UserRepository;
import com.example.how2prompt.modules.identity.service.RefreshTokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenServiceTest extends IdentityIntegrationTestBase {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testCreateVerifyAndRotateToken() {
        User user = new User();
        user.setEmail("testrefresh@example.com");
        user.setFullName("Test Refresh");
        userRepository.save(user);

        // Create
        String rawToken = refreshTokenService.createRefreshToken(user.getId());
        assertNotNull(rawToken);

        // Verify
        RefreshToken token = refreshTokenService.verifyAndGetRefreshToken(rawToken);
        assertNotNull(token);
        assertEquals(user.getId(), token.getUserId());

        // Rotate
        String newRawToken = refreshTokenService.rotateVerifiedToken(token);
        assertNotNull(newRawToken);
        assertNotEquals(rawToken, newRawToken);

        // Old token should be revoked now
        assertThrows(Exception.class, () -> refreshTokenService.verifyAndGetRefreshToken(rawToken));

        // New token should be valid
        RefreshToken newToken = refreshTokenService.verifyAndGetRefreshToken(newRawToken);
        assertNotNull(newToken);
    }
}
