package com.example.how2prompt.modules.identity.repository;

import com.example.how2prompt.modules.identity.entity.User;
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
class UserRepositoryTest {

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
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldNotReturnSoftDeletedUser() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("hashedpassword");
        user.setFullName("Test User");
        user.setLocale("en");
        user.setTimezone("UTC");
        user.setDeletedAt(Instant.now()); // Apply soft delete immediately
        
        entityManager.persistAndFlush(user);
        entityManager.clear(); // Clear L1 cache to force database query

        // Act
        Optional<User> foundById = userRepository.findById(user.getId());
        Optional<User> foundByEmail = userRepository.findByEmail("test@example.com");
        boolean existsByEmail = userRepository.existsByEmail("test@example.com");

        // Assert - The @SQLRestriction("deleted_at IS NULL") should filter out this user
        assertThat(foundById).isEmpty();
        assertThat(foundByEmail).isEmpty();
        assertThat(existsByEmail).isFalse();
    }
}
