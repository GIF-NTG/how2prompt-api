package com.example.how2prompt.modules.catalog.repository;

import com.example.how2prompt.modules.catalog.entity.AiModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiModelRepository extends JpaRepository<AiModel, UUID> {
    List<AiModel> findByIsActiveTrue();
    Optional<AiModel> findByCode(String code);
    boolean existsByCode(String code);
}
