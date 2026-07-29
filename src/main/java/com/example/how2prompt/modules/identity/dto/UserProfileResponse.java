package com.example.how2prompt.modules.identity.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.UUID;

@Getter
@Builder
public class UserProfileResponse {
    private UUID id;
    private String email;
    private String username;
    private String fullName;
    private String avatarUrl;
    private String bio;
    private String locale;
    private String timezone;
}
