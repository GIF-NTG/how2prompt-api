package com.example.how2prompt.modules.identity;

import com.example.how2prompt.common.exception.ConflictException;
import com.example.how2prompt.common.exception.ResourceNotFoundException;
import com.example.how2prompt.modules.identity.dto.UpdateProfileRequest;
import com.example.how2prompt.modules.identity.dto.UserProfileResponse;
import com.example.how2prompt.modules.identity.entity.User;
import com.example.how2prompt.modules.identity.repository.UserRepository;
import com.example.how2prompt.modules.identity.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProfileService profileService;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setFullName("Old Name");
        user.setAvatarUrl("http://old.jpg");
        user.setBio("Old Bio");
        user.setLocale("en_US");
        user.setTimezone("UTC");
    }

    @Test
    void testGetProfile_Success() {
        user.setEmailVerifiedAt(Instant.now());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserProfileResponse response = profileService.getProfile(userId);

        assertEquals(userId, response.getId());
        assertEquals("true", response.getIsEmailVerified());
        assertEquals("test@example.com", response.getEmail());
    }

    @Test
    void testGetProfile_NotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> profileService.getProfile(userId));
    }

    @Test
    void testUpdateProfile_AllFields() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameAndIdNot("newusername", userId)).thenReturn(false);

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("New Name");
        request.setAvatarUrl("http://new.jpg");
        request.setBio("New Bio");
        request.setLocale("vi_VN");
        request.setTimezone("Asia/Ho_Chi_Minh");
        request.setUsername("newusername");

        UserProfileResponse response = profileService.updateProfile(userId, request);

        assertEquals("New Name", response.getFullName());
        assertEquals("http://new.jpg", response.getAvatarUrl());
        assertEquals("New Bio", response.getBio());
        assertEquals("vi_VN", response.getLocale());
        assertEquals("Asia/Ho_Chi_Minh", response.getTimezone());
        assertEquals("newusername", response.getUsername());
        
        verify(userRepository).save(user);
    }

    @Test
    void testUpdateProfile_NullFields() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UpdateProfileRequest request = new UpdateProfileRequest();
        // everything null

        UserProfileResponse response = profileService.updateProfile(userId, request);

        assertEquals("Old Name", response.getFullName());
        assertEquals("http://old.jpg", response.getAvatarUrl());
        assertEquals("Old Bio", response.getBio());
        assertEquals("en_US", response.getLocale());
        assertEquals("UTC", response.getTimezone());
        assertNull(response.getUsername());

        verify(userRepository).save(user);
    }

    @Test
    void testUpdateProfile_EmptyUsername() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername("   ");

        UserProfileResponse response = profileService.updateProfile(userId, request);

        assertNull(response.getUsername());
        verify(userRepository).save(user);
    }

    @Test
    void testUpdateProfile_UsernameConflict() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameAndIdNot("taken", userId)).thenReturn(true);

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername("taken");

        assertThrows(ConflictException.class, () -> profileService.updateProfile(userId, request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testUpdateProfile_NotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        UpdateProfileRequest request = new UpdateProfileRequest();
        assertThrows(ResourceNotFoundException.class, () -> profileService.updateProfile(userId, request));
    }
}
