package com.example.how2prompt.modules.identity.repository;

import com.example.how2prompt.modules.identity.entity.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, UUID> {

    List<WorkspaceMember> findByUserId(UUID userId);
}
