package com.example.how2prompt.modules.identity.service;

import com.example.how2prompt.common.exception.ConflictException;
import com.example.how2prompt.common.exception.ErrorCode;
import com.example.how2prompt.common.exception.UnauthorizedException;
import com.example.how2prompt.infrastructure.security.JwtTokenProvider;
import com.example.how2prompt.modules.identity.dto.ForgotPasswordRequest;
import com.example.how2prompt.modules.identity.dto.GoogleOAuthRequest;
import com.example.how2prompt.modules.identity.dto.LoginRequest;
import com.example.how2prompt.modules.identity.dto.RegisterRequest;
import com.example.how2prompt.modules.identity.dto.RegisterResponse;
import com.example.how2prompt.modules.identity.dto.ResetPasswordRequest;
import com.example.how2prompt.modules.identity.dto.VerifyEmailResponse;
import com.example.how2prompt.modules.identity.entity.RefreshToken;
import com.example.how2prompt.modules.identity.entity.User;
import com.example.how2prompt.modules.identity.entity.UserIdentity;
import com.example.how2prompt.modules.identity.entity.Workspace;
import com.example.how2prompt.modules.identity.entity.WorkspaceType;
import com.example.how2prompt.modules.identity.repository.UserIdentityRepository;
import com.example.how2prompt.modules.identity.repository.UserRepository;
import com.example.how2prompt.modules.identity.repository.WorkspaceRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    public static final String PROVIDER_GOOGLE = "google";

    private final UserRepository userRepository;
    private final UserIdentityRepository userIdentityRepository;
    private final WorkspaceRepository workspaceRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserBootstrapService userBootstrapService;
    private final EmailService emailService;
    private final GoogleIdTokenService googleIdTokenService;
    private final EmailVerificationTokenService emailVerificationTokenService;
    private final PasswordResetTokenService passwordResetTokenService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());

        if (userRepository.existsByEmail(email)) {
            // Generic message — tránh enumeration chi tiết
            throw new ConflictException("Không thể hoàn tất đăng ký.");
        }

        String passwordHash = passwordEncoder.encode(request.getPassword());
        User user = userBootstrapService.createUserWithPersonalWorkspace(
                email,
                passwordHash,
                request.getFullName().trim(),
                null,
                null
        );

        UUID userId = user.getId();
        String fullName = user.getFullName();
        String userEmail = user.getEmail();

        // Redis token + SMTP chỉ sau khi DB commit (tránh orphan token nếu rollback)
        runAfterCommit(() -> issueAndSendVerificationEmail(userId, userEmail, fullName));

        return RegisterResponse.builder()
                .userId(userId)
                .email(userEmail)
                .fullName(fullName)
                .build();
    }

    /**
     * Xác minh email bằng token từ Redis (one-shot).
     */
    @Transactional
    public VerifyEmailResponse verifyEmail(String rawToken) {
        UUID userId = emailVerificationTokenService.consumeToken(rawToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException(
                        ErrorCode.TOKEN_INVALID,
                        "Token xác minh không hợp lệ."
                ));

        if (user.getEmailVerifiedAt() == null) {
            user.setEmailVerifiedAt(Instant.now());
            userRepository.save(user);
        }

        return VerifyEmailResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .verified(true)
                .build();
    }

    /**
     * Gửi lại email verify. Luôn silent (không lộ email có tồn tại hay không).
     */
    public void resendVerification(String rawEmail) {
        String email = normalizeEmail(rawEmail);
        if (!StringUtils.hasText(email)) {
            return;
        }

        if (emailVerificationTokenService.isResendOnCooldown(email)) {
            log.debug("Resend verification cooldown active for {}", email);
            return;
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return;
        }

        User user = userOpt.get();
        if (user.getEmailVerifiedAt() != null) {
            return;
        }

        emailVerificationTokenService.markResendCooldown(email);
        issueAndSendVerificationEmail(user.getId(), user.getEmail(), user.getFullName());
    }

    @Transactional
    public AuthResult login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS));

        if (!StringUtils.hasText(user.getPasswordHash())
                || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (user.getEmailVerifiedAt() == null) {
            throw new UnauthorizedException(
                    ErrorCode.EMAIL_NOT_VERIFIED,
                    "Email chưa được xác minh. Vui lòng kiểm tra hộp thư."
            );
        }

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        return issueTokens(user);
    }

    /**
     * Google OAuth (US-1.2): verify id_token GIS → find/link/create user → JWT nội bộ.
     */
    @Transactional
    public AuthResult loginWithGoogle(GoogleOAuthRequest request) {
        GoogleIdToken.Payload payload = googleIdTokenService.verify(request.getIdToken());

        Boolean emailVerified = payload.getEmailVerified();
        if (!Boolean.TRUE.equals(emailVerified)) {
            throw new UnauthorizedException(
                    ErrorCode.EMAIL_NOT_VERIFIED,
                    "Google email chưa được xác minh."
            );
        }

        String providerUid = payload.getSubject();
        String email = normalizeEmail(payload.getEmail());
        if (!StringUtils.hasText(email)) {
            throw new UnauthorizedException(ErrorCode.TOKEN_INVALID, "Google id_token thiếu email.");
        }

        Map<String, Object> rawProfile = googleIdTokenService.toRawProfile(payload);
        String fullName = stringClaim(payload, "name");
        String avatarUrl = stringClaim(payload, "picture");

        // 1) Tìm theo (provider, provider_uid) TRƯỚC — không tìm email trước
        Optional<UserIdentity> existingIdentity =
                userIdentityRepository.findByProviderAndProviderUid(PROVIDER_GOOGLE, providerUid);

        User user;
        if (existingIdentity.isPresent()) {
            user = existingIdentity.get().getUser();
        } else {
            // 2) Không có identity: link vào user email đã có, hoặc tạo user mới
            Optional<User> existingUser = userRepository.findByEmail(email);
            if (existingUser.isPresent()) {
                user = existingUser.get();
                linkGoogleIdentity(user, providerUid, email, rawProfile);
                if (user.getEmailVerifiedAt() == null) {
                    user.setEmailVerifiedAt(Instant.now());
                }
                if (!StringUtils.hasText(user.getAvatarUrl()) && StringUtils.hasText(avatarUrl)) {
                    user.setAvatarUrl(avatarUrl);
                }
                if (!StringUtils.hasText(user.getFullName()) && StringUtils.hasText(fullName)) {
                    user.setFullName(fullName);
                }
                userRepository.save(user);
            } else {
                user = userBootstrapService.createUserWithPersonalWorkspace(
                        email,
                        null,
                        StringUtils.hasText(fullName) ? fullName : email,
                        avatarUrl,
                        Instant.now()
                );
                linkGoogleIdentity(user, providerUid, email, rawProfile);
            }
        }

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        return issueTokens(user);
    }

    @Transactional
    public AuthResult refresh(String rawRefreshToken) {
        // Rotate: verify + revoke cũ + cấp refresh mới (chống replay)
        RefreshToken existing = refreshTokenService.verifyAndGetRefreshToken(rawRefreshToken);
        UUID userId = existing.getUserId();
        User user = existing.getUser();
        if (user == null) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new UnauthorizedException(ErrorCode.TOKEN_INVALID));
        }

        String newRawRefresh = refreshTokenService.rotateVerifiedToken(existing);
        UUID workspaceId = resolvePersonalWorkspaceId(user.getId());
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(),
                user.getEmail(),
                workspaceId,
                user.isAdmin()
        );
        return new AuthResult(accessToken, newRawRefresh, user.getId());
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenService.revokeRefreshToken(rawRefreshToken);
    }

    private AuthResult issueTokens(User user) {
        UUID workspaceId = resolvePersonalWorkspaceId(user.getId());
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(),
                user.getEmail(),
                workspaceId,
                user.isAdmin()
        );
        String rawRefreshToken = refreshTokenService.createRefreshToken(user.getId());
        return new AuthResult(accessToken, rawRefreshToken, user.getId());
    }

    private void linkGoogleIdentity(
            User user,
            String providerUid,
            String email,
            Map<String, Object> rawProfile
    ) {
        UserIdentity identity = new UserIdentity();
        identity.setUser(user);
        identity.setProvider(PROVIDER_GOOGLE);
        identity.setProviderUid(providerUid);
        identity.setEmail(email);
        identity.setRawProfile(rawProfile);
        userIdentityRepository.save(identity);
    }

    private UUID resolvePersonalWorkspaceId(UUID userId) {
        return workspaceRepository.findFirstByOwner_IdAndType(userId, WorkspaceType.PERSONAL)
                .map(Workspace::getId)
                .orElse(null);
    }

    private void issueAndSendVerificationEmail(UUID userId, String email, String fullName) {
        try {
            String rawToken = emailVerificationTokenService.createToken(userId);
            emailService.sendEmailVerification(email, fullName, rawToken);
        } catch (Exception e) {
            log.error("Failed to issue/send verification email for user {}: {}", userId, e.getMessage(), e);
        }
    }

    private static void runAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private static String stringClaim(GoogleIdToken.Payload payload, String key) {
        Object value = payload.get(key);
        return value == null ? null : value.toString();
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (!StringUtils.hasText(email)) {
            return;
        }

        if (passwordResetTokenService.isResendOnCooldown(email)) {
            log.debug("Forgot password resend cooldown active for {}", email);
            return;
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Thầm lặng bỏ qua để bảo mật tránh khai thác email
            return;
        }

        User user = userOpt.get();
        passwordResetTokenService.markResendCooldown(email);

        UUID userId = user.getId();
        String userEmail = user.getEmail();
        String fullName = user.getFullName();

        runAfterCommit(() -> {
            try {
                String rawToken = passwordResetTokenService.createToken(userId);
                emailService.sendPasswordReset(userEmail, fullName, rawToken);
            } catch (Exception e) {
                log.error("Failed to issue/send password reset email for user {}: {}", userId, e.getMessage(), e);
            }
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        UUID userId = passwordResetTokenService.consumeToken(request.getToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException(
                        ErrorCode.TOKEN_INVALID,
                        "Token khôi phục mật khẩu không hợp lệ."
                ));

        String passwordHash = passwordEncoder.encode(request.getNewPassword());
        user.setPasswordHash(passwordHash);
        userRepository.save(user);

        // Revoke all refresh tokens for this user
        refreshTokenService.revokeAllByUserId(userId);
    }

    public record AuthResult(String accessToken, String refreshToken, UUID userId) {}
}
