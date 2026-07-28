package com.example.how2prompt.modules.identity.repository;

import com.example.how2prompt.modules.identity.entity.User;
import com.example.how2prompt.modules.identity.entity.Workspace;
import com.example.how2prompt.modules.identity.entity.WorkspaceType;
// import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Optional;

import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class WorkspaceRepositoryTest {

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
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldNotReturnSoftDeletedWorkspace() {
        // Arrange
        User user = new User();
        user.setEmail("owner@example.com");
        user.setPasswordHash("hash");
        user.setFullName("Owner User");
        user.setLocale("en");
        user.setTimezone("UTC");
        entityManager.persist(user);

        Workspace workspace = new Workspace();
        workspace.setSlug("test-workspace");
        workspace.setName("Test Workspace");
        workspace.setType(WorkspaceType.PERSONAL);
        workspace.setOwner(user);
        workspace.setDeletedAt(Instant.now()); // Soft delete
        
        entityManager.persistAndFlush(workspace);
        entityManager.clear();

        // Act
        Optional<Workspace> found = workspaceRepository.findById(workspace.getId());

        // Assert - The @SQLRestriction("deleted_at IS NULL") should filter out this workspace
        assertThat(found).isEmpty();
    }
}
