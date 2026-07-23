package com.example.how2prompt.modules.identity.repository;

import com.example.how2prompt.modules.identity.entity.User;
import com.example.how2prompt.modules.identity.entity.Workspace;
import com.example.how2prompt.modules.identity.entity.WorkspaceType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class WorkspaceRepositoryTest {

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
