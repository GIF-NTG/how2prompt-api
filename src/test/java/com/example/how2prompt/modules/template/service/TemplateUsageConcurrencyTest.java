package com.example.how2prompt.modules.template.service;

import com.example.how2prompt.modules.identity.entity.User;
import com.example.how2prompt.modules.identity.entity.Workspace;
import com.example.how2prompt.modules.identity.entity.WorkspaceType;
import com.example.how2prompt.modules.identity.repository.UserRepository;
import com.example.how2prompt.modules.identity.repository.WorkspaceRepository;
import com.example.how2prompt.modules.template.entity.Template;
import com.example.how2prompt.modules.template.repository.TemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.flyway.enabled=true",
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379",
        "spring.data.redis.password="
})
@Testcontainers
@EnabledIfDockerAvailable
@ActiveProfiles("test")
class TemplateUsageConcurrencyTest {

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
    private TemplateUsageService templateUsageService;

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID templateId;

    @BeforeEach
    void setUp() {
        // TRUNCATE CASCADE handles FK dependency chain safely
        jdbcTemplate.execute("TRUNCATE TABLE generated_prompts, template_variants, template_variables, "
                + "template_models, template_categories, template_tags, template_versions, "
                + "templates, workspaces, workspace_members, users CASCADE");

        // 1. Create a dummy user and workspace
        User user = new User();
        user.setEmail("devb@example.com");
        user.setFullName("Dev B");
        user.setAdmin(false);
        user = userRepository.save(user);

        Workspace workspace = new Workspace();
        workspace.setSlug("devb-ws");
        workspace.setName("Dev B Workspace");
        workspace.setType(WorkspaceType.PERSONAL);
        workspace.setOwner(user);
        workspace.setSettings(Map.of());
        workspace = workspaceRepository.save(workspace);

        // 2. Create template with usageCount = 0
        Template template = new Template();
        template.setWorkspaceId(workspace.getId());
        template.setSlug("concurrency-template");
        template.setTitleI18n(Map.of("en", "Concurrency Test"));
        template.setDescriptionI18n(Map.of("en", "Desc"));
        template.setAuthorId(user.getId());
        template.setAuthorType("user");
        template.setOfficial(false);
        template.setPublic(true);
        template.setStatus("published");
        template.setUsageCount(0); // Explicitly start at 0
        template = templateRepository.save(template);

        templateId = template.getId();
    }

    @Test
    void testConcurrentUsageCountIncrements() throws InterruptedException, ExecutionException {
        int numberOfThreads = 30;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latchReady = new CountDownLatch(numberOfThreads);
        CountDownLatch latchStart = new CountDownLatch(1);
        CountDownLatch latchDone = new CountDownLatch(numberOfThreads);

        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < numberOfThreads; i++) {
            tasks.add(() -> {
                latchReady.countDown();
                // Wait for the starting signal so all threads invoke simultaneously
                latchStart.await();
                try {
                    templateUsageService.incrementUsageCount(templateId);
                } finally {
                    latchDone.countDown();
                }
                return null;
            });
        }

        // Submit all tasks to thread pool
        List<Future<Void>> futures = new ArrayList<>();
        for (Callable<Void> task : tasks) {
            futures.add(executorService.submit(task));
        }

        // Wait until all threads are ready
        latchReady.await(5, TimeUnit.SECONDS);

        // Release the barrier (start concurrent execution)
        latchStart.countDown();

        // Wait for all threads to finish processing
        boolean completed = latchDone.await(10, TimeUnit.SECONDS);
        assertThat(completed).isTrue();

        // Verify no exceptions were thrown in any thread
        for (Future<Void> future : futures) {
            future.get();
        }

        // Retrieve template and verify final usageCount
        Template updatedTemplate = templateRepository.findById(templateId).orElseThrow();
        assertThat(updatedTemplate.getUsageCount()).isEqualTo(numberOfThreads);

        // Clean up executor service
        executorService.shutdown();
    }
}
