package com.example.how2prompt.modules.identity;

import com.example.how2prompt.modules.identity.dto.LoginRequest;
import com.example.how2prompt.modules.identity.dto.UpdateProfileRequest;
import com.example.how2prompt.modules.identity.entity.User;
import com.example.how2prompt.modules.identity.repository.UserRepository;
import com.example.how2prompt.modules.identity.repository.WorkspaceRepository;
import com.example.how2prompt.modules.identity.repository.WorkspaceMemberRepository;
import com.example.how2prompt.modules.identity.repository.UserIdentityRepository;
import com.example.how2prompt.modules.identity.repository.RefreshTokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class ProfileIntegrationTest extends IdentityIntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;
    
    @Autowired
    private WorkspaceMemberRepository workspaceMemberRepository;
    
    @Autowired
    private UserIdentityRepository userIdentityRepository;
    
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @MockitoBean
    private GoogleIdTokenVerifier googleIdTokenVerifier;

    @AfterEach
    void tearDown() {
        refreshTokenRepository.deleteAll();
        workspaceMemberRepository.deleteAll();
        workspaceRepository.deleteAll();
        userIdentityRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String getAccessTokenForUser(String email, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Map<?, ?> responseMap = objectMapper.readValue(responseBody, Map.class);
        Map<?, ?> dataMap = (Map<?, ?>) responseMap.get("data");
        return (String) dataMap.get("accessToken");
    }

    @Test
    void testGetMyProfile() throws Exception {
        User user = new User();
        user.setEmail("profile@example.com");
        user.setPasswordHash(passwordEncoder.encode("Password123!"));
        user.setFullName("Profile User");
        user.setEmailVerifiedAt(Instant.now());
        userRepository.save(user);

        String token = getAccessTokenForUser("profile@example.com", "Password123!");

        mockMvc.perform(get("/api/v1/users/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("profile@example.com"))
                .andExpect(jsonPath("$.data.fullName").value("Profile User"));
    }

    @Test
    void testUpdateMyProfile() throws Exception {
        User user = new User();
        user.setEmail("update@example.com");
        user.setPasswordHash(passwordEncoder.encode("Password123!"));
        user.setFullName("Old Name");
        user.setEmailVerifiedAt(Instant.now());
        userRepository.save(user);

        String token = getAccessTokenForUser("update@example.com", "Password123!");

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("New Name");
        request.setBio("New Bio");
        request.setAvatarUrl("http://example.com/avatar.jpg");

        mockMvc.perform(patch("/api/v1/users/me")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("New Name"))
                .andExpect(jsonPath("$.data.bio").value("New Bio"))
                .andExpect(jsonPath("$.data.avatarUrl").value("http://example.com/avatar.jpg"));

        User saved = userRepository.findByEmail("update@example.com").orElse(null);
        assertNotNull(saved);
        assertEquals("New Name", saved.getFullName());
        assertEquals("New Bio", saved.getBio());
    }
}
