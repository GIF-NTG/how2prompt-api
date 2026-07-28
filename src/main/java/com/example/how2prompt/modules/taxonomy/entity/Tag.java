package com.example.how2prompt.modules.taxonomy.entity;

import com.example.how2prompt.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "tags")
@Getter
@Setter
public class Tag extends BaseEntity {

    @Column(nullable = false, unique = true, length = 60)
    private String slug;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(name = "usage_count", nullable = false)
    private Integer usageCount = 0;

    @Column(name = "created_at", insertable = false, updatable = false, nullable = false)
    private Instant createdAt;
}
