package com.example.how2prompt.modules.identity.repository;

import com.example.how2prompt.modules.identity.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {
}
