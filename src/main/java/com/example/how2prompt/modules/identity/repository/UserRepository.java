package com.example.how2prompt.modules.identity.repository;

import com.example.how2prompt.modules.identity.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByUsernameAndIdNot(String username, UUID id);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(u) FROM User u WHERE u.lastLoginAt >= :threshold")
    long countActiveUsersSince(@org.springframework.data.repository.query.Param("threshold") java.time.Instant threshold);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(u) FROM User u WHERE u.emailVerifiedAt IS NOT NULL")
    long countVerifiedUsers();
}
