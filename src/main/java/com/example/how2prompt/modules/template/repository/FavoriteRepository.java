package com.example.how2prompt.modules.template.repository;

import com.example.how2prompt.modules.template.entity.Favorite;
import com.example.how2prompt.modules.template.entity.FavoriteId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId> {

    boolean existsByIdUserIdAndIdTemplateId(UUID userId, UUID templateId);

    void deleteByIdUserIdAndIdTemplateId(UUID userId, UUID templateId);

    List<Favorite> findByIdUserId(UUID userId);
}
