package com.example.how2prompt.modules.identity;

import com.example.how2prompt.modules.identity.entity.User;
import com.example.how2prompt.modules.identity.repository.UserRepository;
import com.example.how2prompt.modules.identity.service.PasswordResetTokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PasswordResetTokenServiceTest extends IdentityIntegrationTestBase {

    @Autowired
    private PasswordResetTokenService passwordResetTokenService;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void testCreateAndConsumeToken_Success() {
        User user = new User();
        user.setEmail("testreset@example.com");
        user.setFullName("Test Reset");
        userRepository.save(user);

        String rawToken = passwordResetTokenService.createToken(user.getId());
        assertNotNull(rawToken);

        UUID consumedUserId = passwordResetTokenService.consumeToken(rawToken);
        assertEquals(user.getId(), consumedUserId);
    }
}
