package com.example.how2prompt.modules.identity.entity;

import com.example.how2prompt.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

/**
 * Bảng {@code user_identities} — liên kết OAuth (US-1.2 Google, mở rộng sau).
 * Đăng nhập email/password không đi qua bảng này.
 * <p>
 * Không soft-delete; không có {@code updated_at} trong schema Flyway — chỉ
 * {@code created_at}.
 */
@Entity
@Table(
        name = "user_identities",
        uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "provider_uid"})
)
@Getter
@Setter
public class UserIdentity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** vd. "google". */
    @Column(name = "provider", nullable = false, length = 30)
    private String provider;

    /** "sub" claim từ provider (Google user id). */
    @Column(name = "provider_uid", nullable = false, length = 255)
    private String providerUid;

    @Column(name = "email", length = 255)
    private String email;

    /** Payload thô từ provider. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_profile", columnDefinition = "jsonb")
    private Map<String, Object> rawProfile;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
