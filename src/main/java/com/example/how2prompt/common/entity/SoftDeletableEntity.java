package com.example.how2prompt.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Khớp pattern audit + soft-delete của Flyway: created_at, updated_at, deleted_at
 * (users, workspaces, ...). Timestamp do DB default/trigger quản lý nên
 * insertable/updatable = false.
 */
@MappedSuperclass
@Getter
@Setter
public abstract class SoftDeletableEntity extends BaseEntity {

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
