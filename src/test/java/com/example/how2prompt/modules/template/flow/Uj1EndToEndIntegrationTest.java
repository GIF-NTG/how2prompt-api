package com.example.how2prompt.modules.template.flow;

import com.example.how2prompt.common.response.PageResponse;
import com.example.how2prompt.common.security.AuthenticatedUser;
import com.example.how2prompt.modules.catalog.dto.request.CreateAiModelRequest;
import com.example.how2prompt.modules.catalog.dto.response.AiModelResponse;
import com.example.how2prompt.modules.catalog.service.AiModelAdminService;
import com.example.how2prompt.modules.identity.entity.User;
import com.example.how2prompt.modules.identity.entity.Workspace;
import com.example.how2prompt.modules.identity.entity.WorkspaceType;
import com.example.how2prompt.modules.identity.repository.UserRepository;
import com.example.how2prompt.modules.identity.repository.WorkspaceRepository;
import com.example.how2prompt.modules.taxonomy.dto.request.CreateCategoryRequest;
import com.example.how2prompt.modules.taxonomy.dto.request.CreateTagRequest;
import com.example.how2prompt.modules.taxonomy.dto.response.CategoryTreeResponse;
import com.example.how2prompt.modules.taxonomy.dto.response.TagResponse;
import com.example.how2prompt.modules.taxonomy.service.TaxonomyAdminService;
import com.example.how2prompt.modules.template.dto.CreateTemplateRequest;
import com.example.how2prompt.modules.template.dto.CreateVariableRequest;
import com.example.how2prompt.modules.template.dto.CreateVariantRequest;
import com.example.how2prompt.modules.template.dto.GeneratePromptRequest;
import com.example.how2prompt.modules.template.dto.TemplateResponse;
import com.example.how2prompt.modules.template.dto.UpdateTemplateRequest;
import com.example.how2prompt.modules.template.dto.request.TemplateSearchCriteria;
import com.example.how2prompt.modules.template.dto.response.TemplateDetailResponse;
import com.example.how2prompt.modules.template.dto.response.TemplateSummaryResponse;
import com.example.how2prompt.modules.template.entity.Template;
import com.example.how2prompt.modules.template.repository.TemplateRepository;
import com.example.how2prompt.modules.template.service.TemplateAdminService;
import com.example.how2prompt.modules.template.service.TemplateQueryService;
import com.example.how2prompt.modules.prompt.entity.GeneratedPrompt;
import com.example.how2prompt.modules.prompt.repository.GeneratedPromptRepository;
import tools.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.MvcResult;
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
class Uj1EndToEndIntegrationTest {

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
    private TemplateQueryService templateQueryService;

    @Autowired
    private AiModelAdminService aiModelAdminService;

    @Autowired
    private TaxonomyAdminService taxonomyAdminService;

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

    private AuthenticatedUser adminUserContext;
    private AuthenticatedUser regularUserContext;

    private AiModelResponse modelGpt4;
    private CategoryTreeResponse categoryCoding;
    private TagResponse tagSpring;

    @BeforeEach
    void setUp() {
        // TRUNCATE CASCADE handles FK dependency chain safely
        jdbcTemplate.execute("TRUNCATE TABLE generated_prompts, template_variants, template_variables, "
                + "template_models, template_categories, template_tags, template_versions, "
                + "templates, workspaces, workspace_members, users, ai_models, tags, categories CASCADE");

        // 1. Create Admin User & Personal Workspace
        User admin = new User();
        admin.setEmail("admin@example.com");
        admin.setFullName("Admin User");
        admin.setAdmin(true);
        admin = userRepository.save(admin);

        Workspace adminWs = new Workspace();
        adminWs.setSlug("admin-ws");
        adminWs.setName("Admin Personal Workspace");
        adminWs.setType(WorkspaceType.PERSONAL);
        adminWs.setOwner(admin);
        adminWs.setSettings(Map.of());
        adminWs = workspaceRepository.save(adminWs);

        adminUserContext = new AuthenticatedUser(admin.getId(), admin.getEmail(), adminWs.getId(), true);

        // 2. Create Regular User & Personal Workspace
        User user = new User();
        user.setEmail("user@example.com");
        user.setFullName("Regular User");
        user.setAdmin(false);
        user = userRepository.save(user);

        Workspace userWs = new Workspace();
        userWs.setSlug("user-ws");
        userWs.setName("User Personal Workspace");
        userWs.setType(WorkspaceType.PERSONAL);
        userWs.setOwner(user);
        userWs.setSettings(Map.of());
        userWs = workspaceRepository.save(userWs);

        regularUserContext = new AuthenticatedUser(user.getId(), user.getEmail(), userWs.getId(), false);

        // 3. Setup Catalog & Taxonomy
        CreateAiModelRequest modelReq = new CreateAiModelRequest();
        modelReq.setCode("gpt-4");
        modelReq.setName("GPT-4");
        modelReq.setProvider("OpenAI");
        modelReq.setModelType("LLM");
        modelGpt4 = aiModelAdminService.create(modelReq);

        CreateCategoryRequest catReq = new CreateCategoryRequest();
        catReq.setSlug("coding");
        catReq.setNameI18n(Map.of("en", "Coding"));
        categoryCoding = taxonomyAdminService.createCategory(catReq);

        CreateTagRequest tagReq = new CreateTagRequest();
        tagReq.setSlug("spring");
        tagReq.setName("Spring Framework");
        tagSpring = taxonomyAdminService.createTag(tagReq);
    }

    @Test
    void executeUj1EndToEndFlow() throws Exception {
        // Step 1: Admin creates a new template
        CreateTemplateRequest createReq = new CreateTemplateRequest();
        createReq.setSlug("java-expert");
        createReq.setTitleI18n(Map.of("en", "Java Expert Prompt"));
        createReq.setDescriptionI18n(Map.of("en", "Assists with Java code"));
        createReq.setPromptBody("Write a {{language}} class to perform {{action}}.");
        createReq.setSystemPrompt("You are an expert programmer.");
        createReq.setExampleOutput("public class Hello {}");
        createReq.setGuideI18n(Map.of("en", "Provide action and language."));

        TemplateResponse templateResponse = templateAdminService.createTemplate(createReq, adminUserContext);
        UUID templateId = templateResponse.id();
        assertThat(templateId).isNotNull();

        // Step 2: Add variables to the template version
        CreateVariableRequest varLang = new CreateVariableRequest();
        varLang.setVarKey("language");
        varLang.setLabelI18n(Map.of("en", "Programming Language"));
        varLang.setInputType("text");
        varLang.setRequired(true);
        varLang.setDefaultValue("Java");
        templateAdminService.addVariable(templateId, varLang);

        CreateVariableRequest varAction = new CreateVariableRequest();
        varAction.setVarKey("action");
        varAction.setLabelI18n(Map.of("en", "Class Action"));
        varAction.setInputType("textarea");
        varAction.setRequired(true);
        templateAdminService.addVariable(templateId, varAction);

        // Step 3: Add an AI model variant override
        CreateVariantRequest variantReq = new CreateVariantRequest();
        variantReq.setAiModelId(modelGpt4.getId());
        variantReq.setPromptBodyOverride("[GPT-4 Mode] Write a {{language}} class to perform {{action}}.");
        variantReq.setSystemPromptOverride("[GPT-4 System] You are a helpful assistant.");
        templateAdminService.addVariant(templateId, variantReq);

        // Step 4: Publish and make it public
        UpdateTemplateRequest updateReq = new UpdateTemplateRequest();
        updateReq.setStatus("published");
        updateReq.setIsPublic(true);
        updateReq.setCategoryIds(List.of(categoryCoding.getId()));
        updateReq.setTagIds(List.of(tagSpring.getId()));
        updateReq.setModelIds(List.of(modelGpt4.getId()));
        templateAdminService.updateTemplate(templateId, updateReq);

        // Step 5: User searches for the template by full-text search
        TemplateSearchCriteria searchCriteria = new TemplateSearchCriteria();
        searchCriteria.setSearch("Expert");
        searchCriteria.setCategory("coding");
        PageResponse<TemplateSummaryResponse> searchResults = templateQueryService.search(searchCriteria, false);
        assertThat(searchResults.getItems()).isNotEmpty();
        assertThat(searchResults.getItems().get(0).getId()).isEqualTo(templateId);

        // Step 6: User retrieves template details
        TemplateDetailResponse detail = templateQueryService.getDetail(templateId, regularUserContext.userId(), false);
        assertThat(detail.getSlug()).isEqualTo("java-expert");
        assertThat(detail.getCurrentVersion().getVariables()).hasSize(2);
        assertThat(detail.getCurrentVersion().getVariants()).hasSize(1);

        // Step 7: User generates prompt using controller endpoint
        GeneratePromptRequest generateRequest = new GeneratePromptRequest();
        generateRequest.setAiModelId(modelGpt4.getId());
        generateRequest.setInputValues(Map.of(
                "language", "Java",
                "action", "connect to DB"
        ));
        generateRequest.setExtraInstructions("Include comments.");
        generateRequest.setTitle("My DB Connection Prompt");

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(regularUserContext, null, authorities);

        MvcResult result = mockMvc.perform(post("/api/v1/templates/{id}/generate", templateId)
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(generateRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertThat(responseContent).contains("[GPT-4 Mode] Write a Java class to perform connect to DB.");
        assertThat(responseContent).contains("Include comments.");

        // Step 8: Verify history is saved and template usage count incremented
        Template updatedTemplate = templateRepository.findById(templateId).orElseThrow();
        assertThat(updatedTemplate.getUsageCount()).isEqualTo(1);

        List<GeneratedPrompt> histories = generatedPromptRepository.findAll().stream()
                .filter(h -> h.getUserId().equals(regularUserContext.userId()))
                .toList();
        assertThat(histories).hasSize(1);
        GeneratedPrompt history = histories.get(0);
        assertThat(history.getTemplateId()).isEqualTo(templateId);
        assertThat(history.getAiModelId()).isEqualTo(modelGpt4.getId());
        assertThat(history.getFinalPrompt()).contains("[GPT-4 Mode] Write a Java class to perform connect to DB.");
        assertThat(history.getInputValues()).containsEntry("language", "Java");
        assertThat(history.getInputValues()).containsEntry("action", "connect to DB");
        assertThat(history.getTitle()).isEqualTo("My DB Connection Prompt");
    }
}
