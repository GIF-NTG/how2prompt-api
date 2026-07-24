package com.example.how2prompt.modules.identity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleOAuthRequest {

    @NotBlank(message = "id_token is required")
    private String idToken;
}
