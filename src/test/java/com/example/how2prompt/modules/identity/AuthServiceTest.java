package com.example.how2prompt.modules.identity;

import com.example.how2prompt.common.exception.ConflictException;
import com.example.how2prompt.common.exception.UnauthorizedException;
import com.example.how2prompt.infrastructure.security.JwtTokenProvider;
import com.example.how2prompt.modules.identity.dto.*;
import com.example.how2prompt.modules.identity.entity.*;
import com.example.how2prompt.modules.identity.repository.*;
import com.example.how2prompt.modules.identity.service.*;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserIdentityRepository userIdentityRepository;
    @Mock private WorkspaceRepository workspaceRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private UserBootstrapService userBootstrapService;
    @Mock private EmailService emailService;
    @Mock private GoogleIdTokenService googleIdTokenService;
    @Mock private EmailVerificationTokenService emailVerificationTokenService;
    @Mock private PasswordResetTokenService passwordResetTokenService;

    @InjectMocks
    private AuthService authService;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setPasswordHash("hashed");
        user.setFullName("Test User");
    }

    @Test
    void testRegister_Conflict() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@example.com");
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(req));
    }

    @Test
    void testVerifyEmail_NotFound() {
        when(emailVerificationTokenService.consumeToken("raw")).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> authService.verifyEmail("raw"));
    }

    @Test
    void testResendVerification_EmptyEmail() {
        authService.resendVerification("");
        verify(emailVerificationTokenService, never()).isResendOnCooldown(any());
    }

    @Test
    void testResendVerification_CooldownActive() {
        when(emailVerificationTokenService.isResendOnCooldown("test@example.com")).thenReturn(true);
        authService.resendVerification("test@example.com");
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void testResendVerification_UserNotFound() {
        when(emailVerificationTokenService.isResendOnCooldown("test@example.com")).thenReturn(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        authService.resendVerification("test@example.com");
        verify(emailVerificationTokenService, never()).markResendCooldown(any());
    }

    @Test
    void testResendVerification_AlreadyVerified() {
        user.setEmailVerifiedAt(Instant.now());
        when(emailVerificationTokenService.isResendOnCooldown("test@example.com")).thenReturn(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        
        authService.resendVerification("test@example.com");
        
        verify(emailVerificationTokenService, never()).markResendCooldown(any());
    }

    @Test
    void testLogin_NotFound() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("pass");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> authService.login(req));
    }

    @Test
    void testLogin_InvalidPassword() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("pass");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass", "hashed")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(req));
    }

    @Test
    void testLogin_NotVerified() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("pass");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass", "hashed")).thenReturn(true);
        // emailVerifiedAt is null

        assertThrows(UnauthorizedException.class, () -> authService.login(req));
    }

    @Test
    void testLoginWithGoogle_EmailNotVerified() {
        GoogleOAuthRequest req = new GoogleOAuthRequest();
        req.setIdToken("token");
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmailVerified(false);
        when(googleIdTokenService.verify("token")).thenReturn(payload);

        assertThrows(UnauthorizedException.class, () -> authService.loginWithGoogle(req));
    }

    @Test
    void testLoginWithGoogle_NoEmail() {
        GoogleOAuthRequest req = new GoogleOAuthRequest();
        req.setIdToken("token");
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmailVerified(true);
        payload.setEmail(null);
        when(googleIdTokenService.verify("token")).thenReturn(payload);

        assertThrows(UnauthorizedException.class, () -> authService.loginWithGoogle(req));
    }

    @Test
    void testLoginWithGoogle_ExistingIdentity() {
        GoogleOAuthRequest req = new GoogleOAuthRequest();
        req.setIdToken("token");
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmailVerified(true);
        payload.setEmail("test@example.com");
        payload.setSubject("google-123");
        
        when(googleIdTokenService.verify("token")).thenReturn(payload);
        
        UserIdentity identity = new UserIdentity();
        identity.setUser(user);
        when(userIdentityRepository.findByProviderAndProviderUid(AuthService.PROVIDER_GOOGLE, "google-123"))
                .thenReturn(Optional.of(identity));
        
        when(workspaceRepository.findFirstByOwner_IdAndType(userId, WorkspaceType.PERSONAL))
                .thenReturn(Optional.empty());
        when(jwtTokenProvider.generateAccessToken(eq(userId), eq("test@example.com"), eq(null), anyBoolean()))
                .thenReturn("access");
        when(refreshTokenService.createRefreshToken(userId)).thenReturn("refresh");

        AuthService.AuthResult res = authService.loginWithGoogle(req);
        
        assertEquals("access", res.accessToken());
        assertEquals("refresh", res.refreshToken());
        verify(userRepository).save(user); // lastLoginAt updated
    }

    @Test
    void testLoginWithGoogle_ExistingUserLink() {
        GoogleOAuthRequest req = new GoogleOAuthRequest();
        req.setIdToken("token");
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmailVerified(true);
        payload.setEmail("test@example.com");
        payload.setSubject("google-123");
        payload.set("name", "New Name");
        payload.set("picture", "http://new.jpg");
        
        when(googleIdTokenService.verify("token")).thenReturn(payload);
        
        when(userIdentityRepository.findByProviderAndProviderUid(AuthService.PROVIDER_GOOGLE, "google-123"))
                .thenReturn(Optional.empty());
        
        user.setFullName(null);
        user.setAvatarUrl(null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        
        when(workspaceRepository.findFirstByOwner_IdAndType(userId, WorkspaceType.PERSONAL))
                .thenReturn(Optional.empty());

        authService.loginWithGoogle(req);
        
        assertNotNull(user.getEmailVerifiedAt());
        assertEquals("New Name", user.getFullName());
        assertEquals("http://new.jpg", user.getAvatarUrl());
        verify(userIdentityRepository).save(any(UserIdentity.class));
    }

    @Test
    void testRefresh_UserNotFound() {
        RefreshToken rt = new RefreshToken();
        rt.setUserId(userId);
        // rt.setUser(null) is default
        when(refreshTokenService.verifyAndGetRefreshToken("raw")).thenReturn(rt);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> authService.refresh("raw"));
    }
    
    @Test
    void testForgotPassword_EmptyEmail() {
        ForgotPasswordRequest req = new ForgotPasswordRequest();
        req.setEmail("");
        authService.forgotPassword(req);
        verify(passwordResetTokenService, never()).isResendOnCooldown(any());
    }

    @Test
    void testForgotPassword_Cooldown() {
        ForgotPasswordRequest req = new ForgotPasswordRequest();
        req.setEmail("test@example.com");
        when(passwordResetTokenService.isResendOnCooldown("test@example.com")).thenReturn(true);
        
        authService.forgotPassword(req);
        
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void testForgotPassword_UserNotFound() {
        ForgotPasswordRequest req = new ForgotPasswordRequest();
        req.setEmail("test@example.com");
        when(passwordResetTokenService.isResendOnCooldown("test@example.com")).thenReturn(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        
        authService.forgotPassword(req);
        
        verify(passwordResetTokenService, never()).markResendCooldown(any());
    }

    @Test
    void testResetPassword_InvalidToken() {
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("invalid");
        when(passwordResetTokenService.consumeToken("invalid")).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        assertThrows(UnauthorizedException.class, () -> authService.resetPassword(req));
    }
}
