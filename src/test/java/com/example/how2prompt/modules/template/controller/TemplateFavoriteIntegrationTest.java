package com.example.how2prompt.modules.template.controller;

import com.example.how2prompt.common.security.AuthenticatedUser;
import com.example.how2prompt.modules.identity.entity.User;
import com.example.how2prompt.modules.identity.entity.Workspace;
import com.example.how2prompt.modules.identity.entity.WorkspaceType;
import com.example.how2prompt.modules.identity.repository.UserRepository;
import com.example.how2prompt.modules.identity.repository.WorkspaceRepository;
import com.example.how2prompt.modules.template.dto.CreateTemplateRequest;
import com.example.how2prompt.modules.template.dto.TemplateResponse;
import com.example.how2prompt.modules.template.entity.Template;
import com.example.how2prompt.modules.template.repository.FavoriteRepository;
import com.example.how2prompt.modules.template.repository.TemplateRepository;
import com.example.how2prompt.modules.template.service.TemplateAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class TemplateFavoriteIntegrationTest {

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
    private TemplateRepository templateRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private TemplateAdminService templateAdminService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private AuthenticatedUser userContext;
    private UsernamePasswordAuthenticationToken auth;
    private UUID templateId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("TRUNCATE TABLE favorites, templates, workspaces, users CASCADE");

        // 1. Setup User and Workspace
        User user = new User();
        user.setEmail("user-fav@example.com");
        user.setFullName("User Fav");
        user.setAdmin(false);
        user = userRepository.save(user);

        Workspace workspace = new Workspace();
        workspace.setSlug("fav-ws");
        workspace.setName("Fav Workspace");
        workspace.setType(WorkspaceType.PERSONAL);
        workspace.setOwner(user);
        workspace.setSettings(Map.of());
        workspace = workspaceRepository.save(workspace);

        userContext = new AuthenticatedUser(user.getId(), user.getEmail(), workspace.getId(), false);
        auth = new UsernamePasswordAuthenticationToken(userContext, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

        // 2. Create template and publish it so it is searchable
        CreateTemplateRequest createReq = new CreateTemplateRequest();
        createReq.setSlug("test-fav-template");
        createReq.setTitleI18n(Map.of("en", "Test Favorite Template"));
        createReq.setPromptBody("Greeting");
        TemplateResponse templateRes = templateAdminService.createTemplate(createReq, userContext);
        templateId = templateRes.id();

        // Publish template
        Template template = templateRepository.findById(templateId).orElseThrow();
        template.setStatus("published");
        template.setPublic(true);
        templateRepository.save(template);
    }

    @Test
    void favoriteAndUnfavoriteTemplateJourney() throws Exception {
        // 1. Verify initially not favorited
        mockMvc.perform(get("/api/v1/templates/" + templateId)
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.favorited").value(false))
                .andExpect(jsonPath("$.data.favoriteCount").value(0));

        // 2. Favorite template
        mockMvc.perform(post("/api/v1/templates/" + templateId + "/favorite")
                        .with(authentication(auth)))
                .andExpect(status().isOk());

        // Verify counts incremented and marked favorited
        mockMvc.perform(get("/api/v1/templates/" + templateId)
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.favorited").value(true))
                .andExpect(jsonPath("$.data.favoriteCount").value(1));

        // Verify favorited in search listing
        mockMvc.perform(get("/api/v1/templates")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].favorited").value(true))
                .andExpect(jsonPath("$.data.items[0].favoriteCount").value(1));

        // 3. Filter search by favoritesOnly
        mockMvc.perform(get("/api/v1/templates?favoritesOnly=true")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].id").value(templateId.toString()));

        // 4. Unfavorite template
        mockMvc.perform(delete("/api/v1/templates/" + templateId + "/favorite")
                        .with(authentication(auth)))
                .andExpect(status().isNoContent());

        // Verify status reverted
        mockMvc.perform(get("/api/v1/templates/" + templateId)
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.favorited").value(false))
                .andExpect(jsonPath("$.data.favoriteCount").value(0));

        // Filter search returns empty
        mockMvc.perform(get("/api/v1/templates?favoritesOnly=true")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(0));
    }
}
