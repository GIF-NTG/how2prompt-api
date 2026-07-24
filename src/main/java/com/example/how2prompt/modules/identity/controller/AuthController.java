package com.example.how2prompt.modules.identity.controller;

import com.example.how2prompt.common.response.ApiResponse;
import com.example.how2prompt.modules.identity.dto.AuthResponse;
import com.example.how2prompt.modules.identity.dto.LoginRequest;
import com.example.how2prompt.modules.identity.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 30 days in seconds
    private static final long REFRESH_COOKIE_MAX_AGE = 30 * 24 * 60 * 60L;
    // 15 mins
    private static final long ACCESS_TOKEN_EXPIRES_IN = 15 * 60L;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthService.AuthResult result = authService.login(request);

        ResponseCookie cookie = createRefreshCookie(result.refreshToken(), REFRESH_COOKIE_MAX_AGE);

        AuthResponse responseBody = AuthResponse.builder()
                .accessToken(result.accessToken())
                .expiresIn(ACCESS_TOKEN_EXPIRES_IN)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.of(responseBody));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @CookieValue(name = "refresh_token", required = true) String refreshToken) {

        AuthService.AuthResult result = authService.refresh(refreshToken);

        AuthResponse responseBody = AuthResponse.builder()
                .accessToken(result.accessToken())
                .expiresIn(ACCESS_TOKEN_EXPIRES_IN)
                .build();

        return ResponseEntity.ok(ApiResponse.of(responseBody));
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

    private ResponseCookie createRefreshCookie(String token, long maxAge) {
        return ResponseCookie.from("refresh_token", token)
                .httpOnly(true)
                .secure(true) // Should be true in prod, but keeping it secure
                .sameSite("Strict")
                .path("/api/v1/auth") // Restrict cookie to auth paths
                .maxAge(maxAge)
                .build();
    }
}