package com.example.how2prompt.modules.identity.repository;

import com.example.how2prompt.modules.identity.entity.Workspace;
import com.example.how2prompt.modules.identity.entity.WorkspaceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {

    boolean existsBySlug(String slug);

    Optional<Workspace> findFirstByOwner_IdAndType(UUID ownerId, WorkspaceType type);
}
