package com.example.how2prompt.modules.identity.service;

import com.example.how2prompt.common.exception.ConflictException;
import com.example.how2prompt.infrastructure.security.JwtTokenProvider;
import com.example.how2prompt.modules.identity.dto.RegisterRequest;
import com.example.how2prompt.modules.identity.dto.RegisterResponse;
import com.example.how2prompt.modules.identity.entity.User;
import com.example.how2prompt.modules.identity.repository.UserIdentityRepository;
import com.example.how2prompt.modules.identity.repository.UserRepository;
import com.example.how2prompt.modules.identity.repository.WorkspaceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    // --- All 10 dependencies matching AuthService's constructor order ---

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserIdentityRepository userIdentityRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private UserBootstrapService userBootstrapService;

    @Mock
    private EmailService emailService;

    @Mock
    private GoogleIdTokenService googleIdTokenService;

    @Mock
    private EmailVerificationTokenService emailVerificationTokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_success() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("Password123!");
        request.setFullName("Test User");

        UUID userId = UUID.randomUUID();
        User bootstrappedUser = new User();
        bootstrappedUser.setId(userId);
        bootstrappedUser.setEmail("test@example.com");
        bootstrappedUser.setFullName("Test User");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("hashed-pwd");
        when(userBootstrapService.createUserWithPersonalWorkspace(
                eq("test@example.com"),
                eq("hashed-pwd"),
                eq("Test User"),
                isNull(),
                isNull()
        )).thenReturn(bootstrappedUser);

        // Act
        RegisterResponse result = authService.register(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getFullName()).isEqualTo("Test User");

        verify(passwordEncoder).encode("Password123!");
        verify(userBootstrapService).createUserWithPersonalWorkspace(
                eq("test@example.com"),
                eq("hashed-pwd"),
                eq("Test User"),
                isNull(),
                isNull()
        );
    }

    @Test
    void register_emailAlreadyExists_throwsException() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("Password123!");
        request.setFullName("Test User");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(ConflictException.class, () -> authService.register(request));
        verify(userBootstrapService, never()).createUserWithPersonalWorkspace(
                any(), any(), any(), any(), any()
        );
    }
}
