package com.example.how2prompt.modules.taxonomy.repository;

import com.example.how2prompt.modules.taxonomy.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Optional<Category> findBySlug(String slug);
    List<Category> findAllByIsActiveTrueOrderBySortOrderAsc();
    boolean existsBySlug(String slug);
}
