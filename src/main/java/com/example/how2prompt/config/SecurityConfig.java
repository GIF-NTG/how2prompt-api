package com.example.how2prompt.config;

import com.example.how2prompt.common.exception.ErrorCode;
import com.example.how2prompt.common.response.ErrorResponse;
import com.example.how2prompt.infrastructure.security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Stateless JWT filter chain. Auth endpoints (register/login/refresh/logout/oauth)
 * public; mọi API khác yêu cầu Bearer access token — TRỪ nhóm endpoint browse/taxonomy
 * (Guest được xem template/category/tag/ai-model không cần đăng nhập) và
 * {@code POST /templates/{id}/generate}, endpoint này cho Guest gọi được nhưng vẫn
 * phải parse token nếu FE có gửi (để tính quota theo user thay vì theo IP) —
 * xem {@link JwtAuthFilter} (optional-auth: không ném lỗi khi thiếu token trên các
 * path nằm trong {@link #PUBLIC_PATHS} / {@link #OPTIONAL_AUTH_PATHS}).
 * <p>
 * Google OAuth dùng luồng GIS id_token (POST /auth/oauth/google) — không bật
 * Spring OAuth2 Login / Authorization Code trên backend.
 * <p>
 * {@link EnableMethodSecurity} bật {@code @PreAuthorize} (vd. admin template CRUD).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final int BCRYPT_COST = 12;

    /** Không cần token, không phân biệt user/guest. */
    private static final String[] PUBLIC_PATHS = {
            "/api/v1/auth/**",
            "/api/v1/categories",
            "/api/v1/categories/**",
            "/api/v1/tags",
            "/api/v1/ai-models",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/v3/api-docs",
            "/swagger-resources/**",
            "/webjars/**",
            "/actuator/health"
    };

    /** Guest gọi được (không bắt buộc token), nhưng nếu có Bearer hợp lệ thì vẫn
     *  phải authenticate — vd. GET /templates trả is_favorited theo user,
     *  POST /templates/{id}/generate tính quota theo user thay vì theo IP. */
    private static final String[] OPTIONAL_AUTH_GET_PATHS = {
            "/api/v1/templates",
            "/api/v1/templates/{id}",
            "/api/v1/templates/featured",
            "/api/v1/templates/trending"
    };

    private static final String OPTIONAL_AUTH_GENERATE_PATH = "/api/v1/templates/{id}/generate";

    private final JwtAuthFilter jwtAuthFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(BCRYPT_COST);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .requestMatchers(HttpMethod.GET, OPTIONAL_AUTH_GET_PATHS).permitAll()
                        .requestMatchers(HttpMethod.POST, OPTIONAL_AUTH_GENERATE_PATH).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                writeError(response, ErrorCode.UNAUTHORIZED))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeError(response, ErrorCode.FORBIDDEN))
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void writeError(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(
                ErrorResponse.of(errorCode.getCode(), errorCode.getDefaultMessage())
        ));
    }
}