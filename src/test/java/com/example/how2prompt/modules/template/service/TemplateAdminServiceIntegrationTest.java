package com.example.how2prompt.modules.template.service;

import com.example.how2prompt.common.security.AuthenticatedUser;
import com.example.how2prompt.modules.catalog.service.AiModelQueryService;
import com.example.how2prompt.modules.identity.entity.User;
import com.example.how2prompt.modules.identity.entity.Workspace;
import com.example.how2prompt.modules.identity.entity.WorkspaceType;
import com.example.how2prompt.modules.identity.repository.UserRepository;
import com.example.how2prompt.modules.identity.repository.WorkspaceRepository;
import com.example.how2prompt.modules.template.dto.CreateTemplateRequest;
import com.example.how2prompt.modules.template.dto.TemplateResponse;
import com.example.how2prompt.modules.template.entity.Template;
import com.example.how2prompt.modules.template.entity.TemplateVersion;
import com.example.how2prompt.modules.template.repository.TemplateRepository;
import com.example.how2prompt.modules.template.repository.TemplateVersionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test: createTemplate tạo đúng Template + TemplateVersion v1 + current_version_id.
 */
@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TemplateAdminService.class, AiModelQueryService.class})
@Testcontainers
class TemplateAdminServiceIntegrationTest {

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
    private TemplateAdminService templateAdminService;

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private TemplateVersionRepository templateVersionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    private AuthenticatedUser currentUser;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setEmail("admin-template@example.com");
        user.setFullName("Admin Template");
        user.setAdmin(true);
        user = userRepository.save(user);

        Workspace workspace = new Workspace();
        workspace.setSlug("admin-ws-" + user.getId().toString().substring(0, 8));
        workspace.setName("Admin Workspace");
        workspace.setType(WorkspaceType.PERSONAL);
        workspace.setOwner(user);
        workspace.setSettings(Map.of());
        workspace = workspaceRepository.save(workspace);

        currentUser = new AuthenticatedUser(user.getId(), user.getEmail(), workspace.getId(), true);
    }

    @Test
    void createTemplate_persistsTemplateAndCurrentVersionV1() {
        CreateTemplateRequest request = new CreateTemplateRequest();
        request.setSlug("hello-world-prompt");
        request.setTitleI18n(Map.of("en", "Hello World", "vi", "Xin chào"));
        request.setDescriptionI18n(Map.of("en", "A simple greeting prompt"));
        request.setPromptBody("Say hello to {{name}}");
        request.setSystemPrompt("You are a helpful assistant.");
        request.setExampleOutput("Hello, Alice!");
        request.setGuideI18n(Map.of("en", "Fill in the name."));

        TemplateResponse response = templateAdminService.createTemplate(request, currentUser);

        assertThat(response.id()).isNotNull();
        assertThat(response.slug()).isEqualTo("hello-world-prompt");
        assertThat(response.status()).isEqualTo("draft");
        assertThat(response.workspaceId()).isEqualTo(currentUser.workspaceId());
        assertThat(response.authorId()).isEqualTo(currentUser.userId());
        assertThat(response.currentVersionId()).isNotNull();

        Template saved = templateRepository.findById(response.id()).orElseThrow();
        assertThat(saved.getCurrentVersionId()).isEqualTo(response.currentVersionId());
        assertThat(saved.getStatus()).isEqualTo("draft");
        assertThat(saved.getTitleI18n()).containsEntry("en", "Hello World");

        TemplateVersion version = templateVersionRepository.findById(response.currentVersionId()).orElseThrow();
        assertThat(version.getVersionNumber()).isEqualTo(1);
        assertThat(version.isCurrent()).isTrue();
        assertThat(version.getPromptBody()).isEqualTo("Say hello to {{name}}");
        assertThat(version.getSystemPrompt()).isEqualTo("You are a helpful assistant.");
        assertThat(version.getCreatedBy()).isEqualTo(currentUser.userId());
        assertThat(version.getTemplate().getId()).isEqualTo(saved.getId());

        assertThat(templateVersionRepository.findByTemplateIdAndCurrentTrue(saved.getId()))
                .isPresent()
                .get()
                .extracting(TemplateVersion::getId)
                .isEqualTo(response.currentVersionId());
    }
}
