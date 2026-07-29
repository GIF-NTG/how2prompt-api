package com.example.how2prompt.modules.identity.controller;

import com.example.how2prompt.common.response.ApiResponse;
import com.example.how2prompt.common.security.AuthenticatedUser;
import com.example.how2prompt.common.security.CurrentUser;
import com.example.how2prompt.modules.identity.dto.UpdateProfileRequest;
import com.example.how2prompt.modules.identity.dto.UserProfileResponse;
import com.example.how2prompt.modules.identity.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(@CurrentUser AuthenticatedUser currentUser) {
        UserProfileResponse response = profileService.getProfile(currentUser.userId());
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            @CurrentUser AuthenticatedUser currentUser,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UserProfileResponse response = profileService.updateProfile(currentUser.userId(), request);
        return ResponseEntity.ok(ApiResponse.of(response));
    }
}
