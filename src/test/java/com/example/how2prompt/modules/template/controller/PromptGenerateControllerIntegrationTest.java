package com.example.how2prompt.modules.template.controller;

import com.example.how2prompt.common.security.AuthenticatedUser;
import com.example.how2prompt.modules.catalog.dto.request.CreateAiModelRequest;
import com.example.how2prompt.modules.catalog.dto.response.AiModelResponse;
import com.example.how2prompt.modules.catalog.service.AiModelAdminService;
import com.example.how2prompt.modules.identity.entity.User;
import com.example.how2prompt.modules.identity.entity.Workspace;
import com.example.how2prompt.modules.identity.entity.WorkspaceType;
import com.example.how2prompt.modules.identity.repository.UserRepository;
import com.example.how2prompt.modules.identity.repository.WorkspaceRepository;
import com.example.how2prompt.modules.template.dto.CreateTemplateRequest;
import com.example.how2prompt.modules.template.dto.CreateVariableRequest;
import com.example.how2prompt.modules.template.dto.GeneratePromptRequest;
import com.example.how2prompt.modules.template.dto.TemplateResponse;
import com.example.how2prompt.modules.template.dto.UpdateTemplateRequest;
import com.example.how2prompt.modules.template.entity.Template;
import com.example.how2prompt.modules.template.repository.TemplateRepository;
import com.example.how2prompt.modules.template.service.TemplateAdminService;
import com.example.how2prompt.modules.prompt.entity.GeneratedPrompt;
import com.example.how2prompt.modules.prompt.repository.GeneratedPromptRepository;
import tools.jackson.databind.ObjectMapper;
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
class PromptGenerateControllerIntegrationTest {

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
    private TemplateAdminService templateAdminService;

    @Autowired
    private AiModelAdminService aiModelAdminService;

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private GeneratedPromptRepository generatedPromptRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private AuthenticatedUser userContext;
    private UUID templateId;
    private UUID modelId;

    @BeforeEach
    void setUp() {
        // TRUNCATE CASCADE handles FK dependency chain safely
        jdbcTemplate.execute("TRUNCATE TABLE generated_prompts, template_variants, template_variables, "
                + "template_models, template_categories, template_tags, template_versions, "
                + "templates, workspaces, workspace_members, users, ai_models CASCADE");

        // 1. Setup User and Workspace
        User user = new User();
        user.setEmail("devc@example.com");
        user.setFullName("Dev C");
        user.setAdmin(false);
        user = userRepository.save(user);

        Workspace workspace = new Workspace();
        workspace.setSlug("devc-ws");
        workspace.setName("Dev C Workspace");
        workspace.setType(WorkspaceType.PERSONAL);
        workspace.setOwner(user);
        workspace.setSettings(Map.of());
        workspace = workspaceRepository.save(workspace);

        userContext = new AuthenticatedUser(user.getId(), user.getEmail(), workspace.getId(), false);

        // 2. Setup AI Model
        CreateAiModelRequest modelReq = new CreateAiModelRequest();
        modelReq.setCode("gpt-3.5-turbo");
        modelReq.setName("GPT 3.5");
        modelReq.setProvider("OpenAI");
        modelReq.setModelType("LLM");
        AiModelResponse modelRes = aiModelAdminService.create(modelReq);
        modelId = modelRes.getId();

        // 3. Create a template
        CreateTemplateRequest createReq = new CreateTemplateRequest();
        createReq.setSlug("form-validation-template");
        createReq.setTitleI18n(Map.of("en", "Validation Template"));
        createReq.setPromptBody("Name: {{name}}, Age: {{age}}");
        createReq.setSystemPrompt("Test system prompt");
        TemplateResponse templateRes = templateAdminService.createTemplate(createReq, userContext);
        templateId = templateRes.id();

        // 4. Add required variables with validation constraints
        CreateVariableRequest varName = new CreateVariableRequest();
        varName.setVarKey("name");
        varName.setLabelI18n(Map.of("en", "Your Name"));
        varName.setInputType("text");
        varName.setRequired(true);
        templateAdminService.addVariable(templateId, varName);

        CreateVariableRequest varAge = new CreateVariableRequest();
        varAge.setVarKey("age");
        varAge.setLabelI18n(Map.of("en", "Your Age"));
        varAge.setInputType("number");
        varAge.setRequired(true);
        varAge.setValidation(Map.of(
                "min", 18,
                "max", 100
        ));
        templateAdminService.addVariable(templateId, varAge);

        // 5. Make it published and public
        UpdateTemplateRequest updateReq = new UpdateTemplateRequest();
        updateReq.setStatus("published");
        updateReq.setIsPublic(true);
        updateReq.setModelIds(List.of(modelId));
        templateAdminService.updateTemplate(templateId, updateReq);
    }

    @Test
    void generate_whenAuthenticated_savesHistoryAndIncrementsUsage() throws Exception {
        GeneratePromptRequest request = new GeneratePromptRequest();
        request.setAiModelId(modelId);
        request.setInputValues(Map.of(
                "name", "Alice",
                "age", 25
        ));

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userContext, null, authorities);

        mockMvc.perform(post("/api/v1/templates/{id}/generate", templateId)
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.finalPrompt").value("Name: Alice, Age: 25"))
                .andExpect(jsonPath("$.data.generatedPromptId").exists())
                .andExpect(jsonPath("$.data.tokensEstimate").exists());

        // Verify history is saved
        List<GeneratedPrompt> histories = generatedPromptRepository.findAll().stream()
                .filter(h -> h.getUserId().equals(userContext.userId()))
                .toList();
        assertThat(histories).hasSize(1);
        assertThat(histories.get(0).getFinalPrompt()).isEqualTo("Name: Alice, Age: 25");

        // Verify usageCount is incremented
        Template template = templateRepository.findById(templateId).orElseThrow();
        assertThat(template.getUsageCount()).isEqualTo(1);
    }

    @Test
    void generate_whenGuest_doesNotSaveHistoryAndDoesNotIncrementUsage() throws Exception {
        GeneratePromptRequest request = new GeneratePromptRequest();
        request.setAiModelId(modelId);
        request.setInputValues(Map.of(
                "name", "Bob",
                "age", 30
        ));

        mockMvc.perform(post("/api/v1/templates/{id}/generate", templateId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.finalPrompt").value("Name: Bob, Age: 30"))
                .andExpect(jsonPath("$.data.generatedPromptId").isEmpty())
                .andExpect(jsonPath("$.data.tokensEstimate").exists());

        // Verify no history saved for user (there is no user anyway, but DB should be empty)
        assertThat(generatedPromptRepository.findAll()).isEmpty();

        // Verify usageCount remains 0 (guest generate does not increment usage_count)
        Template template = templateRepository.findById(templateId).orElseThrow();
        assertThat(template.getUsageCount()).isEqualTo(0);
    }

    @Test
    void generate_whenInputValuesInvalid_returnsValidationError() throws Exception {
        GeneratePromptRequest request = new GeneratePromptRequest();
        request.setAiModelId(modelId);
        request.setInputValues(Map.of(
                "age", 15 // age under 18 (invalid) and missing required "name"
        ));

        mockMvc.perform(post("/api/v1/templates/{id}/generate", templateId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.details.fields").exists());
    }
}
