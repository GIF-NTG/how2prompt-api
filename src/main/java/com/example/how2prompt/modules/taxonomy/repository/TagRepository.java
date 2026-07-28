package com.example.how2prompt.modules.taxonomy.repository;

import com.example.how2prompt.modules.taxonomy.entity.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {
    Optional<Tag> findBySlug(String slug);
    boolean existsBySlug(String slug);

    @Query("SELECT t FROM Tag t ORDER BY t.usageCount DESC")
    List<Tag> findPopular(Pageable pageable);
}
