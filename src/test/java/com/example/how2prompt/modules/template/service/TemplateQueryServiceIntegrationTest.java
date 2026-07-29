package com.example.how2prompt.modules.template.service;

import com.example.how2prompt.common.exception.BadRequestException;
import com.example.how2prompt.common.security.AuthenticatedUser;
import com.example.how2prompt.modules.catalog.dto.request.CreateAiModelRequest;
import com.example.how2prompt.modules.catalog.dto.response.AiModelResponse;
import com.example.how2prompt.modules.catalog.service.AiModelAdminService;
import com.example.how2prompt.modules.catalog.service.AiModelQueryService;
import com.example.how2prompt.modules.identity.entity.User;
import com.example.how2prompt.modules.identity.entity.Workspace;
import com.example.how2prompt.modules.identity.entity.WorkspaceType;
import com.example.how2prompt.modules.identity.repository.UserRepository;
import com.example.how2prompt.modules.identity.repository.WorkspaceRepository;
import com.example.how2prompt.modules.taxonomy.dto.request.CreateCategoryRequest;
import com.example.how2prompt.modules.taxonomy.dto.request.CreateTagRequest;
import com.example.how2prompt.modules.taxonomy.dto.response.CategoryTreeResponse;
import com.example.how2prompt.modules.taxonomy.dto.response.TagResponse;
import com.example.how2prompt.modules.taxonomy.service.CategoryQueryService;
import com.example.how2prompt.modules.taxonomy.service.TagQueryService;
import com.example.how2prompt.modules.taxonomy.service.TaxonomyAdminService;
import com.example.how2prompt.modules.template.dto.CreateTemplateRequest;
import com.example.how2prompt.modules.template.dto.TemplateResponse;
import com.example.how2prompt.modules.template.dto.request.TemplateSearchCriteria;
import com.example.how2prompt.common.response.PageResponse;
import com.example.how2prompt.modules.template.dto.response.TemplateDetailResponse;
import com.example.how2prompt.modules.template.dto.response.TemplateSummaryResponse;
import com.example.how2prompt.modules.template.entity.Template;
import com.example.how2prompt.modules.template.entity.TemplateCategory;
import com.example.how2prompt.modules.template.entity.TemplateCategoryId;
import com.example.how2prompt.modules.template.entity.TemplateModel;
import com.example.how2prompt.modules.template.entity.TemplateModelId;
import com.example.how2prompt.modules.template.entity.TemplateTag;
import com.example.how2prompt.modules.template.entity.TemplateTagId;
import com.example.how2prompt.modules.template.repository.TemplateCategoryRepository;
import com.example.how2prompt.modules.template.repository.TemplateModelRepository;
import com.example.how2prompt.modules.template.repository.TemplateRepository;
import com.example.how2prompt.modules.template.repository.TemplateTagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        TemplateQueryService.class,
        TemplateAdminService.class,
        TemplateUsageService.class,
        AiModelQueryService.class,
        AiModelAdminService.class,
        CategoryQueryService.class,
        TagQueryService.class,
        TaxonomyAdminService.class
})
@Testcontainers
@EnabledIfDockerAvailable
class TemplateQueryServiceIntegrationTest {

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

    @MockitoBean
    private StringRedisTemplate redis;

    @MockitoBean
    private ObjectMapper objectMapper;

    @Autowired
    private TemplateQueryService templateQueryService;

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    @Autowired
    private TemplateAdminService templateAdminService;

    @Autowired
    private AiModelAdminService aiModelAdminService;

    @Autowired
    private TaxonomyAdminService taxonomyAdminService;

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private TemplateCategoryRepository templateCategoryRepository;

    @Autowired
    private TemplateTagRepository templateTagRepository;

    @Autowired
    private TemplateModelRepository templateModelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    private AuthenticatedUser currentUser;
    private TemplateResponse tResponse1;
    private TemplateResponse tResponse2;
    private CategoryTreeResponse catResponse;
    private TagResponse tagResponse;
    private AiModelResponse modelResponse;

    @BeforeEach
    void setUp() {
        // 1. Setup User & Workspace
        User user = new User();
        user.setEmail("search-test@example.com");
        user.setFullName("Search Test");
        user.setAdmin(true);
        user = userRepository.save(user);

        Workspace workspace = new Workspace();
        workspace.setSlug("search-ws-" + user.getId().toString().substring(0, 8));
        workspace.setName("Search Workspace");
        workspace.setType(WorkspaceType.PERSONAL);
        workspace.setOwner(user);
        workspace.setSettings(Map.of());
        workspace = workspaceRepository.save(workspace);

        currentUser = new AuthenticatedUser(user.getId(), user.getEmail(), workspace.getId(), true);

        // 2. Setup AI Model, Category, Tag
        CreateAiModelRequest aiReq = new CreateAiModelRequest();
        aiReq.setCode("gpt-4-test");
        aiReq.setName("GPT 4 Test");
        aiReq.setProvider("OpenAI");
        aiReq.setModelType("LLM");
        modelResponse = aiModelAdminService.create(aiReq);

        CreateCategoryRequest catReq = new CreateCategoryRequest();
        catReq.setSlug("coding-cat");
        catReq.setNameI18n(Map.of("en", "Coding Category"));
        catResponse = taxonomyAdminService.createCategory(catReq);

        CreateTagRequest tagReq = new CreateTagRequest();
        tagReq.setSlug("spring-tag");
        tagReq.setName("Spring Tag");
        tagResponse = taxonomyAdminService.createTag(tagReq);

        // 3. Setup Templates
        CreateTemplateRequest req1 = new CreateTemplateRequest();
        req1.setSlug("java-template");
        req1.setTitleI18n(Map.of("en", "Java code guidelines", "vi", "Hướng dẫn Java"));
        req1.setDescriptionI18n(Map.of("en", "Description for java template"));
        req1.setPromptBody("Java code: {{code}}");
        tResponse1 = templateAdminService.createTemplate(req1, currentUser);

        // Cập nhật template 1 thành published & public
        Template t1 = templateRepository.findById(tResponse1.id()).orElseThrow();
        t1.setStatus("published");
        t1.setPublic(true);
        templateRepository.save(t1);

        CreateTemplateRequest req2 = new CreateTemplateRequest();
        req2.setSlug("python-template");
        req2.setTitleI18n(Map.of("en", "Python script guidelines", "vi", "Hướng dẫn Python"));
        req2.setDescriptionI18n(Map.of("en", "Description for python template"));
        req2.setPromptBody("Python code: {{code}}");
        tResponse2 = templateAdminService.createTemplate(req2, currentUser);

        Template t2 = templateRepository.findById(tResponse2.id()).orElseThrow();
        t2.setStatus("published");
        t2.setPublic(true);
        templateRepository.save(t2);

        // 4. Liên kết Template 1 với Category, Tag, Model
        TemplateCategory tc = new TemplateCategory();
        tc.setId(new TemplateCategoryId(tResponse1.id(), catResponse.getId()));
        tc.setTemplate(t1);
        templateCategoryRepository.save(tc);

        TemplateTag tt = new TemplateTag();
        tt.setId(new TemplateTagId(tResponse1.id(), tagResponse.getId()));
        tt.setTemplate(t1);
        templateTagRepository.save(tt);

        TemplateModel tm = new TemplateModel();
        tm.setId(new TemplateModelId(tResponse1.id(), modelResponse.getId()));
        tm.setTemplate(t1);
        templateModelRepository.save(tm);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void search_templates_withSpecificationFilters() {
        // Lọc theo Category
        TemplateSearchCriteria criteria = new TemplateSearchCriteria();
        criteria.setCategory("coding-cat");
        PageResponse<TemplateSummaryResponse> page = templateQueryService.search(criteria, false);
        assertThat(page.getItems()).hasSize(1);
        assertThat(page.getItems().get(0).getId()).isEqualTo(tResponse1.id());

        // Lọc theo Model
        criteria = new TemplateSearchCriteria();
        criteria.setModel("gpt-4-test");
        page = templateQueryService.search(criteria, false);
        assertThat(page.getItems()).hasSize(1);

        // Lọc theo Tag
        criteria = new TemplateSearchCriteria();
        criteria.setTags(List.of("spring-tag"));
        page = templateQueryService.search(criteria, false);
        assertThat(page.getItems()).hasSize(1);
    }

    @Test
    void search_templates_withFtsQuerySupportsAllSortModes() {
        Template featured = templateRepository.findById(tResponse1.id()).orElseThrow();
        featured.setFeaturedAt(Instant.parse("2026-07-28T00:00:00Z"));
        featured.setUsageCount(10);
        templateRepository.save(featured);
        entityManager.flush();
        entityManager.clear();

        for (String sort : List.of("newest", "trending", "featured")) {
            TemplateSearchCriteria criteria = new TemplateSearchCriteria();
            criteria.setSearch("Java");
            criteria.setSort(sort);

            PageResponse<TemplateSummaryResponse> page = templateQueryService.search(criteria, false);

            assertThat(page.getItems())
                    .as("FTS results sorted by %s", sort)
                    .extracting(TemplateSummaryResponse::getId)
                    .containsExactly(tResponse1.id());
        }
    }

    @Test
    void search_featuredExcludesTemplatesWithoutFeaturedAt() {
        Template featured = templateRepository.findById(tResponse1.id()).orElseThrow();
        featured.setFeaturedAt(Instant.parse("2026-07-28T00:00:00Z"));
        templateRepository.save(featured);
        entityManager.flush();
        entityManager.clear();

        TemplateSearchCriteria criteria = new TemplateSearchCriteria();
        criteria.setSort("featured");

        PageResponse<TemplateSummaryResponse> page = templateQueryService.search(criteria, false);

        assertThat(page.getItems())
                .extracting(TemplateSummaryResponse::getId)
                .containsExactly(tResponse1.id());
    }

    @Test
    void search_rejectsLimitOutsideAllowedRange() {
        TemplateSearchCriteria criteria = new TemplateSearchCriteria();
        criteria.setLimit(0);

        assertThatThrownBy(() -> templateQueryService.search(criteria, false))
                .isInstanceOf(BadRequestException.class);

        criteria.setLimit(101);
        assertThatThrownBy(() -> templateQueryService.search(criteria, false))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void search_templates_pagination_cursor() {
        // Trang 1: limit 1
        TemplateSearchCriteria criteria = new TemplateSearchCriteria();
        criteria.setLimit(1);
        PageResponse<TemplateSummaryResponse> page1 = templateQueryService.search(criteria, false);

        assertThat(page1.getItems()).hasSize(1);
        assertThat(page1.isHasMore()).isTrue();
        assertThat(page1.getNextCursor()).isNotNull();

        // Trang 2: sử dụng cursor từ trang 1
        TemplateSearchCriteria criteria2 = new TemplateSearchCriteria();
        criteria2.setLimit(1);
        criteria2.setCursor(page1.getNextCursor());
        PageResponse<TemplateSummaryResponse> page2 = templateQueryService.search(criteria2, false);

        assertThat(page2.getItems()).hasSize(1);
        assertThat(page2.getItems().get(0).getId()).isNotEqualTo(page1.getItems().get(0).getId());
    }

    @Test
    void getDetail_returnsDetailResponse_success() {
        TemplateDetailResponse detail = templateQueryService.getDetail(tResponse1.id(), currentUser.userId(), true);
        assertThat(detail.getSlug()).isEqualTo("java-template");
        assertThat(detail.getCategories()).hasSize(1);
        assertThat(detail.getCategories().get(0).getSlug()).isEqualTo("coding-cat");
        assertThat(detail.getTags()).hasSize(1);
        assertThat(detail.getTags().get(0).getSlug()).isEqualTo("spring-tag");
        assertThat(detail.getModels()).hasSize(1);
        assertThat(detail.getModels().get(0).getCode()).isEqualTo("gpt-4-test");
    }

    @Test
    void getDetail_foreignCurrentVersionPointerFallsBackToOwnedVersion() {
        Template first = templateRepository.findById(tResponse1.id()).orElseThrow();
        Template second = templateRepository.findById(tResponse2.id()).orElseThrow();
        UUID ownedVersionId = first.getCurrentVersionId();
        first.setCurrentVersionId(second.getCurrentVersionId());
        templateRepository.save(first);
        entityManager.flush();
        entityManager.clear();

        TemplateDetailResponse detail =
                templateQueryService.getDetail(tResponse1.id(), currentUser.userId(), true);

        assertThat(detail.getCurrentVersion().getId()).isEqualTo(ownedVersionId);
    }
}
