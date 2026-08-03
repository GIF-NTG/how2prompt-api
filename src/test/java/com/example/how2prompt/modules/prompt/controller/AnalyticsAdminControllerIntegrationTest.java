package com.example.how2prompt.modules.prompt.controller;

import com.example.how2prompt.common.security.AuthenticatedUser;
import com.example.how2prompt.modules.identity.entity.User;
import com.example.how2prompt.modules.identity.entity.Workspace;
import com.example.how2prompt.modules.identity.entity.WorkspaceType;
import com.example.how2prompt.modules.identity.repository.UserRepository;
import com.example.how2prompt.modules.identity.repository.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.flyway.enabled=true",
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379",
        "spring.data.redis.password="
})
@AutoConfigureMockMvc
@Testcontainers
@EnabledIfDockerAvailable
@ActiveProfiles("test")
class AnalyticsAdminControllerIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("how2prompt_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UsernamePasswordAuthenticationToken adminAuth;
    private UsernamePasswordAuthenticationToken userAuth;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("TRUNCATE TABLE generated_prompts, templates, workspaces, users CASCADE");

        // 1. Setup Admin and User
        User adminUser = new User();
        adminUser.setEmail("admin-analytics@example.com");
        adminUser.setFullName("Admin Analytics");
        adminUser.setAdmin(true);
        adminUser = userRepository.save(adminUser);

        User normalUser = new User();
        normalUser.setEmail("user-analytics@example.com");
        normalUser.setFullName("User Analytics");
        normalUser.setAdmin(false);
        normalUser = userRepository.save(normalUser);

        Workspace workspace = new Workspace();
        workspace.setSlug("analytics-ws");
        workspace.setName("Analytics Workspace");
        workspace.setType(WorkspaceType.PERSONAL);
        workspace.setOwner(adminUser);
        workspace.setSettings(Map.of());
        workspace = workspaceRepository.save(workspace);

        AuthenticatedUser adminContext = new AuthenticatedUser(adminUser.getId(), adminUser.getEmail(), workspace.getId(), true);
        adminAuth = new UsernamePasswordAuthenticationToken(adminContext, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        AuthenticatedUser userContext = new AuthenticatedUser(normalUser.getId(), normalUser.getEmail(), workspace.getId(), false);
        userAuth = new UsernamePasswordAuthenticationToken(userContext, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void getDashboard_asAdmin_returnsDashboardMetrics() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/dashboard")
                        .with(authentication(adminAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dau").exists())
                .andExpect(jsonPath("$.data.wau").exists())
                .andExpect(jsonPath("$.data.mau").exists())
                .andExpect(jsonPath("$.data.promptsGeneratedPerDay").exists())
                .andExpect(jsonPath("$.data.popularTemplates").exists())
                .andExpect(jsonPath("$.data.mostUsedModels").exists())
                .andExpect(jsonPath("$.data.conversionFunnel").exists());
    }

    @Test
    void getDashboard_asUser_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/dashboard")
                        .with(authentication(userAuth)))
                .andExpect(status().isForbidden());
    }
}
