package com.example.how2prompt.modules.identity.repository;

import com.example.how2prompt.modules.identity.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Optional;

import com.example.how2prompt.common.TestcontainersConfig;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfig.class)
class UserRepositoryTest {

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
