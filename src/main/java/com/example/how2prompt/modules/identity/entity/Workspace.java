package com.example.how2prompt.modules.identity.entity;

import com.example.how2prompt.common.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.util.Map;

/**
 * Bảng {@code workspaces} (V2__identity_tenancy).
 * type='personal' tự tạo lúc register (US-1.5).
 */
@Entity
@Table(name = "workspaces")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class Workspace extends SoftDeletableEntity {

    @Column(name = "slug", nullable = false, unique = true, length = 60)
    private String slug;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "type", nullable = false, length = 20)
    private WorkspaceType type = WorkspaceType.PERSONAL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "settings", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> settings = Map.of();
}
