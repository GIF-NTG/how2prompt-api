package com.example.how2prompt.modules.identity.service;

import com.example.how2prompt.common.exception.ConflictException;
import com.example.how2prompt.common.exception.ResourceNotFoundException;
import com.example.how2prompt.modules.identity.dto.UpdateProfileRequest;
import com.example.how2prompt.modules.identity.dto.UserProfileResponse;
import com.example.how2prompt.modules.identity.entity.User;
import com.example.how2prompt.modules.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return mapToResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName().trim());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl().trim());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getLocale() != null) {
            user.setLocale(request.getLocale().trim());
        }
        if (request.getTimezone() != null) {
            user.setTimezone(request.getTimezone().trim());
        }
        if (request.getUsername() != null) {
            String newUsername = request.getUsername().trim();
            if (StringUtils.hasText(newUsername)) {
                if (userRepository.existsByUsernameAndIdNot(newUsername, userId)) {
                    throw new ConflictException("Username đã được sử dụng bởi người dùng khác.");
                }
                user.setUsername(newUsername);
            } else {
                user.setUsername(null);
            }
        }

        userRepository.save(user);
        return mapToResponse(user);
    }

    private UserProfileResponse mapToResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .locale(user.getLocale())
                .timezone(user.getTimezone())
                .isAdmin(String.valueOf(user.isAdmin()))
                .isEmailVerified(String.valueOf(user.getEmailVerifiedAt() != null))
                .build();
    }
}
