package com.example.how2prompt.modules.identity.controller;

import com.example.how2prompt.common.exception.ErrorCode;
import com.example.how2prompt.common.exception.UnauthorizedException;
import com.example.how2prompt.common.response.ApiResponse;
import com.example.how2prompt.config.AuthProperties;
import com.example.how2prompt.config.JwtProperties;
import com.example.how2prompt.modules.identity.dto.AuthResponse;
import com.example.how2prompt.modules.identity.dto.GoogleOAuthRequest;
import com.example.how2prompt.modules.identity.dto.LoginRequest;
import com.example.how2prompt.modules.identity.dto.RegisterRequest;
import com.example.how2prompt.modules.identity.dto.RegisterResponse;
import com.example.how2prompt.modules.identity.dto.ResendVerificationRequest;
import com.example.how2prompt.modules.identity.dto.VerifyEmailRequest;
import com.example.how2prompt.modules.identity.dto.VerifyEmailResponse;
import com.example.how2prompt.modules.identity.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Auth API. Path prefix {@code /api/v1} được gắn bởi {@link com.example.how2prompt.config.WebConfig}.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthProperties authProperties;
    private final JwtProperties jwtProperties;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse body = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(body));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthService.AuthResult result = authService.login(request);
        return authSuccess(result);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<VerifyEmailResponse>> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request) {
        VerifyEmailResponse body = authService.verifyEmail(request.getToken());
        return ResponseEntity.ok(ApiResponse.of(body));
    }

    /**
     * Luôn 202 — không tiết lộ email có tồn tại / đã verify hay chưa.
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<Void> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        authService.resendVerification(request.getEmail());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/oauth/google")
    public ResponseEntity<ApiResponse<AuthResponse>> googleOAuth(@Valid @RequestBody GoogleOAuthRequest request) {
        AuthService.AuthResult result = authService.loginWithGoogle(request);
        return authSuccess(result);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new UnauthorizedException(ErrorCode.TOKEN_INVALID, "Refresh token không hợp lệ.");
        }

        AuthService.AuthResult result = authService.refresh(refreshToken);
        // Rotate: set cookie refresh token mới
        ResponseCookie cookie = createRefreshCookie(result.refreshToken(), refreshCookieMaxAgeSeconds());

        AuthResponse responseBody = AuthResponse.builder()
                .accessToken(result.accessToken())
                .expiresIn(accessTokenExpiresInSeconds())
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.of(responseBody));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refresh_token", required = false) String refreshToken) {

        if (refreshToken != null) {
            authService.logout(refreshToken);
        }

        ResponseCookie cookie = createRefreshCookie("", 0L);

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    private ResponseEntity<ApiResponse<AuthResponse>> authSuccess(AuthService.AuthResult result) {
        ResponseCookie cookie = createRefreshCookie(result.refreshToken(), refreshCookieMaxAgeSeconds());

        AuthResponse responseBody = AuthResponse.builder()
                .accessToken(result.accessToken())
                .expiresIn(accessTokenExpiresInSeconds())
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.of(responseBody));
    }

    private ResponseCookie createRefreshCookie(String token, long maxAgeSeconds) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie
                .from(authProperties.getCookieName(), token)
                .httpOnly(true)
                .secure(authProperties.isCookieSecure())
                .path(authProperties.getCookiePath())
                .maxAge(maxAgeSeconds);

        String sameSite = authProperties.getCookieSameSite();
        if (sameSite != null && !sameSite.isBlank()) {
            builder.sameSite(sameSite);
        }

        return builder.build();
    }

    private long accessTokenExpiresInSeconds() {
        return jwtProperties.getAccessTokenTtl().toSeconds();
    }

    private long refreshCookieMaxAgeSeconds() {
        return jwtProperties.getRefreshTokenTtl().toSeconds();
    }
}
