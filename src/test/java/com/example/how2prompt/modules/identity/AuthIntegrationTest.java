package com.example.how2prompt.modules.identity;

import com.example.how2prompt.modules.identity.dto.LoginRequest;
import com.example.how2prompt.modules.identity.dto.RegisterRequest;
import com.example.how2prompt.modules.identity.entity.User;
import com.example.how2prompt.modules.identity.repository.UserRepository;
import com.example.how2prompt.modules.identity.repository.WorkspaceRepository;
import com.example.how2prompt.modules.identity.repository.WorkspaceMemberRepository;
import com.example.how2prompt.modules.identity.repository.UserIdentityRepository;
import com.example.how2prompt.modules.identity.repository.RefreshTokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class AuthIntegrationTest extends IdentityIntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;
    
    @Autowired
    private WorkspaceMemberRepository workspaceMemberRepository;
    
    @Autowired
    private UserIdentityRepository userIdentityRepository;
    
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @MockitoBean
    private GoogleIdTokenVerifier googleIdTokenVerifier;

    @AfterEach
    void tearDown() {
        refreshTokenRepository.deleteAll();
        workspaceMemberRepository.deleteAll();
        workspaceRepository.deleteAll();
        userIdentityRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testRegister_Success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("testregister@example.com");
        request.setPassword("Password123!");
        request.setFullName("Test Register");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.userId").exists())
                .andExpect(jsonPath("$.data.email").value("testregister@example.com"));

        User saved = userRepository.findByEmail("testregister@example.com").orElse(null);
        assertNotNull(saved);
        assertEquals("Test Register", saved.getFullName());
    }

    @Test
    void testLogin_Success() throws Exception {
        User user = new User();
        user.setEmail("testlogin@example.com");
        user.setPasswordHash(passwordEncoder.encode("Password123!"));
        user.setFullName("Test Login");
        user.setEmailVerifiedAt(Instant.now());
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setEmail("testlogin@example.com");
        request.setPassword("Password123!");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(cookie().exists("refresh_token"));
    }
    @Autowired
    private com.example.how2prompt.modules.identity.service.EmailVerificationTokenService emailVerificationTokenService;

    @Autowired
    private com.example.how2prompt.modules.identity.service.PasswordResetTokenService passwordResetTokenService;

    @Test
    void testVerifyEmail_Success() throws Exception {
        User user = new User();
        user.setEmail("verify@example.com");
        user.setPasswordHash(passwordEncoder.encode("Password123!"));
        user.setFullName("Test Verify");
        userRepository.save(user);

        String rawToken = emailVerificationTokenService.createToken(user.getId());

        com.example.how2prompt.modules.identity.dto.VerifyEmailRequest request = new com.example.how2prompt.modules.identity.dto.VerifyEmailRequest();
        request.setToken(rawToken);

        mockMvc.perform(post("/api/v1/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testResetPassword_Success() throws Exception {
        User user = new User();
        user.setEmail("reset@example.com");
        user.setPasswordHash(passwordEncoder.encode("Password123!"));
        user.setFullName("Test Reset");
        user.setEmailVerifiedAt(Instant.now());
        userRepository.save(user);

        String rawToken = passwordResetTokenService.createToken(user.getId());

        com.example.how2prompt.modules.identity.dto.ResetPasswordRequest request = new com.example.how2prompt.modules.identity.dto.ResetPasswordRequest();
        request.setToken(rawToken);
        request.setNewPassword("NewPassword123!");

        mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testResendVerification() throws Exception {
        com.example.how2prompt.modules.identity.dto.ResendVerificationRequest request = new com.example.how2prompt.modules.identity.dto.ResendVerificationRequest();
        request.setEmail("testregister@example.com");
        mockMvc.perform(post("/api/v1/auth/resend-verification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());
    }

    @Test
    void testForgotPassword() throws Exception {
        com.example.how2prompt.modules.identity.dto.ForgotPasswordRequest request = new com.example.how2prompt.modules.identity.dto.ForgotPasswordRequest();
        request.setEmail("testregister@example.com");
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());
    }

    @Test
    void testRefresh() throws Exception {
        User user = new User();
        user.setEmail("refresh@example.com");
        user.setPasswordHash(passwordEncoder.encode("Password123!"));
        user.setFullName("Test Refresh");
        user.setEmailVerifiedAt(Instant.now());
        userRepository.save(user);

        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("refresh@example.com");
        loginReq.setPassword("Password123!");
        
        String refreshToken = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andReturn().getResponse().getCookie("refresh_token").getValue();

        mockMvc.perform(post("/api/v1/auth/refresh")
                .cookie(new jakarta.servlet.http.Cookie("refresh_token", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(cookie().exists("refresh_token"));
    }

    @Test
    void testLogout() throws Exception {
        User user = new User();
        user.setEmail("logout@example.com");
        user.setPasswordHash(passwordEncoder.encode("Password123!"));
        user.setFullName("Test Logout");
        user.setEmailVerifiedAt(Instant.now());
        userRepository.save(user);

        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("logout@example.com");
        loginReq.setPassword("Password123!");

        String refreshToken = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andReturn().getResponse().getCookie("refresh_token").getValue();

        mockMvc.perform(post("/api/v1/auth/logout")
                .cookie(new jakarta.servlet.http.Cookie("refresh_token", refreshToken)))
                .andExpect(status().isNoContent())
                .andExpect(cookie().exists("refresh_token"));
    }

    @Test
    void testGoogleLogin() throws Exception {
        com.example.how2prompt.modules.identity.dto.GoogleOAuthRequest request = new com.example.how2prompt.modules.identity.dto.GoogleOAuthRequest();
        request.setIdToken("fake-google-token");
        
        com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload payload = new com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload();
        payload.setSubject("fake-google-uid");
        payload.setEmail("google@example.com");
        payload.setEmailVerified(true);
        payload.set("name", "Google User");
        payload.set("picture", "http://google.com/pic.jpg");
        
        com.google.api.client.googleapis.auth.oauth2.GoogleIdToken idToken = new com.google.api.client.googleapis.auth.oauth2.GoogleIdToken(
            new com.google.api.client.json.webtoken.JsonWebSignature.Header(), payload, new byte[0], new byte[0]);
        
        org.mockito.Mockito.when(googleIdTokenVerifier.verify("fake-google-token")).thenReturn(idToken);

        mockMvc.perform(post("/api/v1/auth/oauth/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
