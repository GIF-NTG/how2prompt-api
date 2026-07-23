package com.example.how2prompt.modules.identity.service;

import com.example.how2prompt.common.exception.ConflictException;
import com.example.how2prompt.modules.identity.dto.RegisterRequest;
import com.example.how2prompt.modules.identity.entity.User;
import com.example.how2prompt.modules.identity.entity.Workspace;
import com.example.how2prompt.modules.identity.entity.WorkspaceMember;
import com.example.how2prompt.modules.identity.entity.WorkspaceRole;
import com.example.how2prompt.modules.identity.entity.WorkspaceType;
import com.example.how2prompt.modules.identity.repository.UserRepository;
import com.example.how2prompt.modules.identity.repository.WorkspaceMemberRepository;
import com.example.how2prompt.modules.identity.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email is already in use");
        }

        // 1. Create and Save User
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user = userRepository.save(user);

        // 2. Create and Save Workspace (type=PERSONAL)
        Workspace workspace = new Workspace();
        workspace.setName(user.getFullName() + "'s Workspace");
        // Generate a simple slug
        workspace.setSlug(user.getEmail().split("@")[0] + "-" + System.currentTimeMillis());
        workspace.setType(WorkspaceType.PERSONAL);
        workspace.setOwner(user);
        workspace = workspaceRepository.save(workspace);

        // 3. Create and Save WorkspaceMember (role=OWNER)
        WorkspaceMember member = new WorkspaceMember();
        member.setUser(user);
        member.setWorkspace(workspace);
        member.setRole(WorkspaceRole.OWNER);
        workspaceMemberRepository.save(member);

        // 4. Send Verification Email
        sendVerificationEmail(user.getEmail());

        return user;
    }

    @Async
    public void sendVerificationEmail(String email) {
        log.info("Sending verification email to: {}", email);
        // Dummy implementation for non-blocking email sending
    }
}
