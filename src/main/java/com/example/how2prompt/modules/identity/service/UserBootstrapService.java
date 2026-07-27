package com.example.how2prompt.modules.identity.service;

import com.example.how2prompt.modules.identity.entity.User;
import com.example.how2prompt.modules.identity.entity.Workspace;
import com.example.how2prompt.modules.identity.entity.WorkspaceMember;
import com.example.how2prompt.modules.identity.entity.WorkspaceRole;
import com.example.how2prompt.modules.identity.entity.WorkspaceType;
import com.example.how2prompt.modules.identity.repository.UserRepository;
import com.example.how2prompt.modules.identity.repository.WorkspaceMemberRepository;
import com.example.how2prompt.modules.identity.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Tạo user mới kèm personal workspace + membership owner (US-1.5).
 * Dùng chung cho register email/password và Google OAuth first-login.
 */
@Service
@RequiredArgsConstructor
public class UserBootstrapService {

    private static final int MAX_SLUG_ATTEMPTS = 12;
    private static final int BASE_SLUG_MAX_LEN = 40;

    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    @Transactional
    public User createUserWithPersonalWorkspace(
            String email,
            String passwordHash,
            String fullName,
            String avatarUrl,
            Instant emailVerifiedAt
    ) {
        User user = new User();
        user.setEmail(normalizeEmail(email));
        user.setPasswordHash(passwordHash);
        user.setFullName(fullName);
        user.setAvatarUrl(avatarUrl);
        user.setEmailVerifiedAt(emailVerifiedAt);
        user = userRepository.save(user);

        Workspace workspace = new Workspace();
        workspace.setSlug(generateUniqueSlug(fullName, email));
        workspace.setName(resolveWorkspaceName(fullName, email));
        workspace.setType(WorkspaceType.PERSONAL);
        workspace.setOwner(user);
        workspace.setSettings(Map.of());
        workspace = workspaceRepository.save(workspace);

        WorkspaceMember member = new WorkspaceMember();
        member.setWorkspace(workspace);
        member.setUser(user);
        member.setRole(WorkspaceRole.OWNER);
        workspaceMemberRepository.save(member);

        return user;
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private static String resolveWorkspaceName(String fullName, String email) {
        if (StringUtils.hasText(fullName)) {
            return fullName.trim() + "'s Workspace";
        }
        String local = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        return local + "'s Workspace";
    }

    private String generateUniqueSlug(String fullName, String email) {
        String source = StringUtils.hasText(fullName) ? fullName : email;
        String base = slugify(source);
        if (!StringUtils.hasText(base)) {
            base = "user";
        }
        if (base.length() > BASE_SLUG_MAX_LEN) {
            base = base.substring(0, BASE_SLUG_MAX_LEN);
        }

        String candidate = base;
        for (int i = 0; i < MAX_SLUG_ATTEMPTS; i++) {
            if (!workspaceRepository.existsBySlug(candidate)) {
                return candidate;
            }
            candidate = base + "-" + UUID.randomUUID().toString().substring(0, 8);
        }
        return base + "-" + UUID.randomUUID();
    }

    private static String slugify(String raw) {
        String s = raw.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return s;
    }
}
