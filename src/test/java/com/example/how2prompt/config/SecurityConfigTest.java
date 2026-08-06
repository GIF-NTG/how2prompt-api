package com.example.how2prompt.config;

import com.example.how2prompt.infrastructure.security.JwtAuthFilter;
import org.junit.jupiter.api.Test;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import org.mockito.ArgumentCaptor;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;

class SecurityConfigTest {

    @Test
    void passwordEncoder() {
        JwtAuthFilter jwtAuthFilter = mock(JwtAuthFilter.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        
        SecurityConfig config = new SecurityConfig(jwtAuthFilter, objectMapper);
        PasswordEncoder encoder = config.passwordEncoder();
        
        assertNotNull(encoder);
    }
    
    @Test
    void filterChain() throws Exception {
        JwtAuthFilter jwtAuthFilter = mock(JwtAuthFilter.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        
        SecurityConfig config = new SecurityConfig(jwtAuthFilter, objectMapper);
        
        HttpSecurity http = mock(HttpSecurity.class);
        when(http.csrf(any(Customizer.class))).thenReturn(http);
        when(http.cors(any(Customizer.class))).thenReturn(http);
        when(http.sessionManagement(any(Customizer.class))).thenReturn(http);
        when(http.authorizeHttpRequests(any(Customizer.class))).thenReturn(http);
        when(http.exceptionHandling(any(Customizer.class))).thenReturn(http);
        when(http.addFilterBefore(any(), any())).thenReturn(http);
        
        DefaultSecurityFilterChain mockChain = mock(DefaultSecurityFilterChain.class);
        when(http.build()).thenReturn(mockChain);
        
        
        SecurityFilterChain chain = config.filterChain(http);
        assertNotNull(chain);
        
        // Capture the ExceptionHandling customizer
        ArgumentCaptor<Customizer> exceptionHandlingCaptor = ArgumentCaptor.forClass(Customizer.class);
        verify(http).exceptionHandling(exceptionHandlingCaptor.capture());
        
        ExceptionHandlingConfigurer exceptionConfigurer = mock(ExceptionHandlingConfigurer.class);
        when(exceptionConfigurer.authenticationEntryPoint(any())).thenReturn(exceptionConfigurer);
        when(exceptionConfigurer.accessDeniedHandler(any())).thenReturn(exceptionConfigurer);
        
        exceptionHandlingCaptor.getValue().customize(exceptionConfigurer);
        
        // Capture EntryPoint
        ArgumentCaptor<AuthenticationEntryPoint> entryPointCaptor = ArgumentCaptor.forClass(AuthenticationEntryPoint.class);
        verify(exceptionConfigurer).authenticationEntryPoint(entryPointCaptor.capture());
        
        // Capture AccessDeniedHandler
        ArgumentCaptor<AccessDeniedHandler> deniedHandlerCaptor = ArgumentCaptor.forClass(AccessDeniedHandler.class);
        verify(exceptionConfigurer).accessDeniedHandler(deniedHandlerCaptor.capture());
        
        // Invoke them to hit the writeError calls inside the lambdas
        jakarta.servlet.http.HttpServletResponse response = mock(jakarta.servlet.http.HttpServletResponse.class);
        java.io.PrintWriter writer = mock(java.io.PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);
        
        entryPointCaptor.getValue().commence(mock(jakarta.servlet.http.HttpServletRequest.class), response, mock(org.springframework.security.core.AuthenticationException.class));
        deniedHandlerCaptor.getValue().handle(mock(jakarta.servlet.http.HttpServletRequest.class), response, mock(org.springframework.security.access.AccessDeniedException.class));
    }
}
