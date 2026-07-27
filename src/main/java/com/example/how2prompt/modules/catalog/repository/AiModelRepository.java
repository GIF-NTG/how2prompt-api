package com.example.how2prompt.modules.catalog.repository;

import com.example.how2prompt.modules.catalog.entity.AiModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AiModelRepository extends JpaRepository<AiModel, UUID> {
    List<AiModel> findByIsActiveTrue();
    boolean existsByCode(String code);
}
