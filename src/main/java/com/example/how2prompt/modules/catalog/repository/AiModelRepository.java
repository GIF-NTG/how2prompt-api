package com.example.how2prompt.modules.catalog.repository;

import com.example.how2prompt.modules.catalog.entity.AiModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AiModelRepository extends JpaRepository<AiModel, UUID> {

    Optional<AiModel> findByCode(String code);

    boolean existsByIdAndActiveTrue(UUID id);
}
