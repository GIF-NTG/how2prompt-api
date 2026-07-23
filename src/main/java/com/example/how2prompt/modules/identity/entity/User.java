package com.example.how2prompt.modules.identity.entity;

import com.example.how2prompt.common.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;

/**
 * Bảng {@code users} (V2__identity_tenancy).
 */
@Entity
@Table(name = "users")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class User extends SoftDeletableEntity {

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "full_name", length = 150)
    private String fullName;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "locale", nullable = false, length = 10)
    private String locale = "en";

    @Column(name = "timezone", length = 50)
    private String timezone = "Asia/Ho_Chi_Minh";

    @Column(name = "is_admin", nullable = false)
    private boolean admin = false;

    @Column(name = "email_verified_at")
    private Instant emailVerifiedAt;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;
}
