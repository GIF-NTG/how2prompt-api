package com.example.how2prompt.modules.identity.service;

import com.example.how2prompt.common.exception.UnauthorizedException;
import com.example.how2prompt.infrastructure.security.JwtTokenProvider;
import com.example.how2prompt.modules.identity.dto.LoginRequest;
import com.example.how2prompt.modules.identity.entity.RefreshToken;
import com.example.how2prompt.modules.identity.entity.User;
import com.example.how2prompt.modules.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    // We can fetch default workspace logic here, keeping it null for now
    // as per instructions, or we could fetch the personal workspace.
    // Assuming personal workspace id logic will be handled by Dev B.

    @Transactional
    public AuthResult login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        // Issue access token
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(),
                user.getEmail(),
                null, // workspaceId will be addressed later/by Dev B
                false // admin flag
        );

        // Issue refresh token (raw token string)
        String rawRefreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new AuthResult(accessToken, rawRefreshToken, user.getId());
    }

    @Transactional
    public AuthResult refresh(String rawRefreshToken) {
        RefreshToken refreshTokenEntity = refreshTokenService.verifyAndGetRefreshToken(rawRefreshToken);

        User user = refreshTokenEntity.getUser();

        // Issue new access token
        String newAccessToken = jwtTokenProvider.generateAccessToken(
                user.getId(),
                user.getEmail(),
                null,
                false
        );

        return new AuthResult(newAccessToken, rawRefreshToken, user.getId());
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            refreshTokenService.revokeRefreshToken(rawRefreshToken);
        }
    }

    public record AuthResult(String accessToken, String refreshToken, UUID userId) {}
}