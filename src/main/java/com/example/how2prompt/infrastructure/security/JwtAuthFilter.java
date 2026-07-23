package com.example.how2prompt.infrastructure.security;

import com.example.how2prompt.common.security.AuthenticatedUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length());
            try {
                Claims claims = jwtTokenProvider.parse(token).getPayload();

                if (jwtTokenProvider.isAccessToken(claims)) {
                    AuthenticatedUser principal = new AuthenticatedUser(
                            jwtTokenProvider.extractUserId(claims),
                            jwtTokenProvider.extractEmail(claims),
                            jwtTokenProvider.extractWorkspaceId(claims),
                            jwtTokenProvider.extractAdmin(claims)
                    );

                    List<GrantedAuthority> authorities = principal.admin()
                            ? List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"))
                            : List.of(new SimpleGrantedAuthority("ROLE_USER"));

                    var authToken = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    // Refresh token gửi nhầm lên endpoint thường -> không set Authentication.
                    log.debug("Nhận refresh token ở nơi cần access token, bỏ qua.");
                }
            } catch (JwtException | IllegalArgumentException e) {
                log.debug("JWT không hợp lệ: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
