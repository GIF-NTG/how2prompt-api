package com.example.how2prompt.modules.identity;

import com.example.how2prompt.modules.identity.entity.User;
import com.example.how2prompt.modules.identity.repository.UserRepository;
import com.example.how2prompt.modules.identity.service.EmailVerificationTokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EmailVerificationTokenServiceTest extends IdentityIntegrationTestBase {

    @Autowired
    private EmailVerificationTokenService emailVerificationTokenService;

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
        user.setEmail("testverify@example.com");
        user.setFullName("Test Verify");
        userRepository.save(user);

        String rawToken = emailVerificationTokenService.createToken(user.getId());
        assertNotNull(rawToken);

        UUID consumedUserId = emailVerificationTokenService.consumeToken(rawToken);
        assertEquals(user.getId(), consumedUserId);
    }
}
