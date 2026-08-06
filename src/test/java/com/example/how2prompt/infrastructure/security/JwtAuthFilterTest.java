package com.example.how2prompt.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_NoHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_InvalidPrefix() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic token");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_NotAccessToken() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer refresh_token");
        
        io.jsonwebtoken.Jws<Claims> jws = mock(io.jsonwebtoken.Jws.class);
        Claims claims = mock(Claims.class);
        when(jws.getPayload()).thenReturn(claims);
        when(jwtTokenProvider.parse("refresh_token")).thenReturn(jws);
        when(jwtTokenProvider.isAccessToken(claims)).thenReturn(false);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ValidToken_Admin() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid_token");
        
        io.jsonwebtoken.Jws<Claims> jws = mock(io.jsonwebtoken.Jws.class);
        Claims claims = mock(Claims.class);
        when(jws.getPayload()).thenReturn(claims);
        when(jwtTokenProvider.parse("valid_token")).thenReturn(jws);
        when(jwtTokenProvider.isAccessToken(claims)).thenReturn(true);
        
        when(jwtTokenProvider.extractUserId(claims)).thenReturn(UUID.randomUUID());
        when(jwtTokenProvider.extractEmail(claims)).thenReturn("admin@test.com");
        when(jwtTokenProvider.extractWorkspaceId(claims)).thenReturn(null);
        when(jwtTokenProvider.extractAdmin(claims)).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ValidToken_User() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid_token_user");
        
        io.jsonwebtoken.Jws<Claims> jws = mock(io.jsonwebtoken.Jws.class);
        Claims claims = mock(Claims.class);
        when(jws.getPayload()).thenReturn(claims);
        when(jwtTokenProvider.parse("valid_token_user")).thenReturn(jws);
        when(jwtTokenProvider.isAccessToken(claims)).thenReturn(true);
        
        when(jwtTokenProvider.extractUserId(claims)).thenReturn(UUID.randomUUID());
        when(jwtTokenProvider.extractEmail(claims)).thenReturn("user@test.com");
        when(jwtTokenProvider.extractWorkspaceId(claims)).thenReturn(null);
        when(jwtTokenProvider.extractAdmin(claims)).thenReturn(false);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_JwtException() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid_token");
        when(jwtTokenProvider.parse("invalid_token")).thenThrow(new io.jsonwebtoken.MalformedJwtException("Invalid token"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
