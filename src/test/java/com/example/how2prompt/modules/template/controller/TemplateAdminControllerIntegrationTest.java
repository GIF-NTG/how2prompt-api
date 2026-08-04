package com.example.how2prompt.modules.template.controller;

import com.example.how2prompt.common.security.AuthenticatedUser;
import com.example.how2prompt.modules.identity.entity.User;
import com.example.how2prompt.modules.identity.entity.Workspace;
import com.example.how2prompt.modules.identity.entity.WorkspaceType;
import com.example.how2prompt.modules.identity.repository.UserRepository;
import com.example.how2prompt.modules.identity.repository.WorkspaceRepository;
import com.example.how2prompt.modules.template.dto.CreateTemplateRequest;
import com.example.how2prompt.modules.template.dto.TemplateResponse;
import com.example.how2prompt.modules.template.service.TemplateAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class TemplateAdminControllerIntegrationTest {

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
    private TemplateAdminService templateAdminService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private AuthenticatedUser adminUserContext;
    private AuthenticatedUser regularUserContext;
    private UsernamePasswordAuthenticationToken adminAuth;
    private UsernamePasswordAuthenticationToken regularAuth;
    private UUID templateId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("TRUNCATE TABLE template_versions, templates, workspaces, users CASCADE");

        // Create Admin User
        User admin = new User();
        admin.setEmail("admin-delete@example.com");
        admin.setFullName("Admin Delete");
        admin.setAdmin(true);
        admin = userRepository.save(admin);

        Workspace adminWorkspace = new Workspace();
        adminWorkspace.setSlug("admin-delete-ws");
        adminWorkspace.setName("Admin Delete Workspace");
        adminWorkspace.setType(WorkspaceType.PERSONAL);
        adminWorkspace.setOwner(admin);
        adminWorkspace.setSettings(Map.of());
        adminWorkspace = workspaceRepository.save(adminWorkspace);

        adminUserContext = new AuthenticatedUser(admin.getId(), admin.getEmail(), adminWorkspace.getId(), true);
        adminAuth = new UsernamePasswordAuthenticationToken(adminUserContext, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        // Create Regular User
        User regular = new User();
        regular.setEmail("regular-delete@example.com");
        regular.setFullName("Regular User");
        regular.setAdmin(false);
        regular = userRepository.save(regular);

        Workspace regularWorkspace = new Workspace();
        regularWorkspace.setSlug("regular-delete-ws");
        regularWorkspace.setName("Regular Workspace");
        regularWorkspace.setType(WorkspaceType.PERSONAL);
        regularWorkspace.setOwner(regular);
        regularWorkspace.setSettings(Map.of());
        regularWorkspace = workspaceRepository.save(regularWorkspace);

        regularUserContext = new AuthenticatedUser(regular.getId(), regular.getEmail(), regularWorkspace.getId(), false);
        regularAuth = new UsernamePasswordAuthenticationToken(regularUserContext, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

        // Create template
        CreateTemplateRequest createReq = new CreateTemplateRequest();
        createReq.setSlug("test-delete-endpoint-template");
        createReq.setTitleI18n(Map.of("en", "Test Endpoint Delete"));
        createReq.setPromptBody("Greeting");
        TemplateResponse templateRes = templateAdminService.createTemplate(createReq, adminUserContext);
        templateId = templateRes.id();
    }

    @Test
    void deleteTemplate_returnsForbiddenForRegularUser() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/templates/" + templateId)
                        .with(authentication(regularAuth)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteTemplate_returnsNoContentForAdminUserAndExcludesFromSearch() throws Exception {
        // 1. Delete as admin
        mockMvc.perform(delete("/api/v1/admin/templates/" + templateId)
                        .with(authentication(adminAuth)))
                .andExpect(status().isNoContent());

        // 2. Check if template is excluded from search (returns empty/not found because of soft-delete restriction)
        mockMvc.perform(get("/api/v1/templates/" + templateId)
                        .with(authentication(adminAuth)))
                .andExpect(status().isNotFound());
    }
}
