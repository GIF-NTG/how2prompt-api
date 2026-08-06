package com.example.how2prompt.modules.identity.controller;

import com.example.how2prompt.common.exception.GlobalExceptionHandler;
import com.example.how2prompt.config.AuthProperties;
import com.example.how2prompt.config.JwtProperties;
import com.example.how2prompt.modules.identity.dto.*;
import com.example.how2prompt.modules.identity.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Duration;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthService authService;

    @Mock
    private AuthProperties authProperties;

    @Mock
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void register_ValidRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("Password123");
        request.setFullName("Test User");

        RegisterResponse response = RegisterResponse.builder()
                .userId(UUID.randomUUID())
                .email("test@example.com")
                .fullName("Test User")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    void register_EmptyBody_Returns400() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_ValidRequest_WithSameSite() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("Password123");

        AuthService.AuthResult result = new AuthService.AuthResult("acc", "ref", UUID.randomUUID());
        when(authService.login(any(LoginRequest.class))).thenReturn(result);
        when(jwtProperties.getAccessTokenTtl()).thenReturn(Duration.ofMinutes(15));
        when(jwtProperties.getRefreshTokenTtl()).thenReturn(Duration.ofDays(7));
        when(authProperties.getCookieName()).thenReturn("refresh_token");
        when(authProperties.isCookieSecure()).thenReturn(true);
        when(authProperties.getCookiePath()).thenReturn("/api/v1/auth/refresh");
        when(authProperties.getCookieSameSite()).thenReturn("Strict");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("SameSite=Strict")));
    }

    @Test
    void login_ValidRequest_WithoutSameSite() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("Password123");

        AuthService.AuthResult result = new AuthService.AuthResult("acc", "ref", UUID.randomUUID());
        when(authService.login(any(LoginRequest.class))).thenReturn(result);
        when(jwtProperties.getAccessTokenTtl()).thenReturn(Duration.ofMinutes(15));
        when(jwtProperties.getRefreshTokenTtl()).thenReturn(Duration.ofDays(7));
        when(authProperties.getCookieName()).thenReturn("refresh_token");
        when(authProperties.isCookieSecure()).thenReturn(true);
        when(authProperties.getCookiePath()).thenReturn("/api/v1/auth/refresh");
        when(authProperties.getCookieSameSite()).thenReturn("");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("SameSite"))));
    }

    @Test
    void login_EmptyBody_Returns400() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void verifyEmail_EmptyToken() throws Exception {
        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setToken("");
        mockMvc.perform(post("/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void verifyEmail_ValidToken() throws Exception {
        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setToken("valid");
        when(authService.verifyEmail("valid")).thenReturn(new VerifyEmailResponse());
        mockMvc.perform(post("/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void resendVerification_EmptyEmail() throws Exception {
        ResendVerificationRequest request = new ResendVerificationRequest();
        request.setEmail("");
        mockMvc.perform(post("/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resendVerification_ValidEmail() throws Exception {
        ResendVerificationRequest request = new ResendVerificationRequest();
        request.setEmail("test@test.com");
        mockMvc.perform(post("/auth/resend-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());
    }

    @Test
    void forgotPassword_EmptyEmail() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("");
        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void forgotPassword_ValidEmail() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@test.com");
        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());
    }

    @Test
    void resetPassword_EmptyBody() throws Exception {
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPassword_ValidBody() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("tok");
        request.setNewPassword("Password123");
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void googleOAuth_EmptyBody() throws Exception {
        mockMvc.perform(post("/auth/oauth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void googleOAuth_ValidBody() throws Exception {
        GoogleOAuthRequest request = new GoogleOAuthRequest();
        request.setIdToken("tok");
        AuthService.AuthResult result = new AuthService.AuthResult("acc", "ref", UUID.randomUUID());
        when(authService.loginWithGoogle(any())).thenReturn(result);
        when(jwtProperties.getAccessTokenTtl()).thenReturn(Duration.ofMinutes(15));
        when(jwtProperties.getRefreshTokenTtl()).thenReturn(Duration.ofDays(7));
        when(authProperties.getCookieName()).thenReturn("refresh_token");
        when(authProperties.isCookieSecure()).thenReturn(true);
        when(authProperties.getCookiePath()).thenReturn("/api/v1/auth/refresh");
        when(authProperties.getCookieSameSite()).thenReturn("Strict");

        mockMvc.perform(post("/auth/oauth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void refresh_NoCookie() throws Exception {
        mockMvc.perform(post("/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_BlankCookie() throws Exception {
        mockMvc.perform(post("/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", "  ")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_ValidCookie() throws Exception {
        AuthService.AuthResult result = new AuthService.AuthResult("acc", "ref", UUID.randomUUID());
        when(authService.refresh("valid")).thenReturn(result);
        when(jwtProperties.getAccessTokenTtl()).thenReturn(Duration.ofMinutes(15));
        when(jwtProperties.getRefreshTokenTtl()).thenReturn(Duration.ofDays(7));
        when(authProperties.getCookieName()).thenReturn("refresh_token");
        when(authProperties.isCookieSecure()).thenReturn(true);
        when(authProperties.getCookiePath()).thenReturn("/api/v1/auth/refresh");
        when(authProperties.getCookieSameSite()).thenReturn(null);

        mockMvc.perform(post("/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", "valid")))
                .andExpect(status().isOk());
    }

    @Test
    void logout_NoCookie() throws Exception {
        when(authProperties.getCookieName()).thenReturn("refresh_token");
        when(authProperties.isCookieSecure()).thenReturn(true);
        when(authProperties.getCookiePath()).thenReturn("/api/v1/auth/refresh");

        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isNoContent());
    }

    @Test
    void logout_WithCookie() throws Exception {
        when(authProperties.getCookieName()).thenReturn("refresh_token");
        when(authProperties.isCookieSecure()).thenReturn(true);
        when(authProperties.getCookiePath()).thenReturn("/api/v1/auth/refresh");

        mockMvc.perform(post("/auth/logout")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", "valid")))
                .andExpect(status().isNoContent());
        
        verify(authService).logout("valid");
    }
}
