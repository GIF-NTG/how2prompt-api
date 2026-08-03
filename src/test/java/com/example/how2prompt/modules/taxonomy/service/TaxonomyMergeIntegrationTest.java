package com.example.how2prompt.modules.taxonomy.service;

import com.example.how2prompt.common.security.AuthenticatedUser;
import com.example.how2prompt.modules.identity.entity.User;
import com.example.how2prompt.modules.identity.entity.Workspace;
import com.example.how2prompt.modules.identity.entity.WorkspaceType;
import com.example.how2prompt.modules.identity.repository.UserRepository;
import com.example.how2prompt.modules.identity.repository.WorkspaceRepository;
import com.example.how2prompt.modules.taxonomy.dto.request.CreateTagRequest;
import com.example.how2prompt.modules.taxonomy.dto.request.TagMergeRequest;
import com.example.how2prompt.modules.taxonomy.dto.response.TagResponse;
import com.example.how2prompt.modules.taxonomy.entity.Tag;
import com.example.how2prompt.modules.taxonomy.repository.TagRepository;
import com.example.how2prompt.modules.template.dto.CreateTemplateRequest;
import com.example.how2prompt.modules.template.dto.TemplateResponse;
import com.example.how2prompt.modules.template.entity.Template;
import com.example.how2prompt.modules.template.repository.TemplateRepository;
import com.example.how2prompt.modules.template.repository.TemplateTagRepository;
import com.example.how2prompt.modules.template.service.TemplateAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class TaxonomyMergeIntegrationTest {

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
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private TemplateTagRepository templateTagRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TaxonomyAdminService taxonomyAdminService;

    @Autowired
    private TemplateAdminService templateAdminService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private AuthenticatedUser adminContext;
    private UsernamePasswordAuthenticationToken adminAuth;
    private UsernamePasswordAuthenticationToken userAuth;
    private UUID sourceTagId;
    private UUID targetTagId;
    private UUID templateId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("TRUNCATE TABLE template_tags, templates, tags, workspaces, users CASCADE");

        // 1. Setup Admin and User contexts
        User adminUser = new User();
        adminUser.setEmail("admin-merge@example.com");
        adminUser.setFullName("Admin Merge");
        adminUser.setAdmin(true);
        adminUser = userRepository.save(adminUser);

        User normalUser = new User();
        normalUser.setEmail("user-merge@example.com");
        normalUser.setFullName("User Merge");
        normalUser.setAdmin(false);
        normalUser = userRepository.save(normalUser);

        Workspace workspace = new Workspace();
        workspace.setSlug("merge-ws");
        workspace.setName("Merge Workspace");
        workspace.setType(WorkspaceType.PERSONAL);
        workspace.setOwner(adminUser);
        workspace.setSettings(Map.of());
        workspace = workspaceRepository.save(workspace);

        adminContext = new AuthenticatedUser(adminUser.getId(), adminUser.getEmail(), workspace.getId(), true);
        adminAuth = new UsernamePasswordAuthenticationToken(adminContext, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        AuthenticatedUser userContext = new AuthenticatedUser(normalUser.getId(), normalUser.getEmail(), workspace.getId(), false);
        userAuth = new UsernamePasswordAuthenticationToken(userContext, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

        // 2. Setup Tags
        CreateTagRequest createTag1 = new CreateTagRequest();
        createTag1.setSlug("csharp");
        createTag1.setName("C# Language");
        TagResponse tagRes1 = taxonomyAdminService.createTag(createTag1);
        sourceTagId = tagRes1.getId();

        CreateTagRequest createTag2 = new CreateTagRequest();
        createTag2.setSlug("dotnet");
        createTag2.setName(".NET Core");
        TagResponse tagRes2 = taxonomyAdminService.createTag(createTag2);
        targetTagId = tagRes2.getId();

        // Set initial usage counts
        Tag tag1 = tagRepository.findById(sourceTagId).orElseThrow();
        tag1.setUsageCount(5);
        tagRepository.save(tag1);

        Tag tag2 = tagRepository.findById(targetTagId).orElseThrow();
        tag2.setUsageCount(10);
        tagRepository.save(tag2);

        // 3. Setup Template and link to source tag
        CreateTemplateRequest createReq = new CreateTemplateRequest();
        createReq.setSlug("merge-template");
        createReq.setTitleI18n(Map.of("en", "Merge Template"));
        createReq.setPromptBody("Greeting");
        TemplateResponse templateRes = templateAdminService.createTemplate(createReq, adminContext);
        templateId = templateRes.id();

        // Link template to source tag
        com.example.how2prompt.modules.template.dto.UpdateTemplateRequest updateReq = new com.example.how2prompt.modules.template.dto.UpdateTemplateRequest();
        updateReq.setTagIds(List.of(sourceTagId));
        templateAdminService.updateTemplate(templateId, updateReq);
    }

    @Test
    void mergeTags_asAdmin_succeedsAndUpdatesRelationsAndCounts() throws Exception {
        // Verify initial setup
        assertThat(templateTagRepository.existsByIdTemplateIdAndIdTagId(templateId, sourceTagId)).isTrue();
        assertThat(templateTagRepository.existsByIdTemplateIdAndIdTagId(templateId, targetTagId)).isFalse();

        TagMergeRequest request = new TagMergeRequest();
        request.setSourceTagId(sourceTagId);
        request.setTargetTagId(targetTagId);

        // Perform merge
        mockMvc.perform(post("/api/v1/admin/tags/merge")
                        .with(authentication(adminAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Verify source tag deleted
        assertThat(tagRepository.findById(sourceTagId)).isEmpty();

        // Verify target tag usage count updated (5 + 10 = 15)
        Tag targetTag = tagRepository.findById(targetTagId).orElseThrow();
        assertThat(targetTag.getUsageCount()).isEqualTo(15);

        // Verify template association updated to target tag
        assertThat(templateTagRepository.existsByIdTemplateIdAndIdTagId(templateId, sourceTagId)).isFalse();
        assertThat(templateTagRepository.existsByIdTemplateIdAndIdTagId(templateId, targetTagId)).isTrue();
    }

    @Test
    void mergeTags_asUser_returnsForbidden() throws Exception {
        TagMergeRequest request = new TagMergeRequest();
        request.setSourceTagId(sourceTagId);
        request.setTargetTagId(targetTagId);

        mockMvc.perform(post("/api/v1/admin/tags/merge")
                        .with(authentication(userAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
