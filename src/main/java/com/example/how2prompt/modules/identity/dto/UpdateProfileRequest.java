package com.example.how2prompt.modules.identity.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {
    @Size(max = 150, message = "Full name không được vượt quá 150 ký tự.")
    private String fullName;

    @Size(max = 500, message = "Avatar URL không được vượt quá 500 ký tự.")
    private String avatarUrl;

    private String bio;

    @Size(max = 10, message = "Locale không được vượt quá 10 ký tự.")
    private String locale;

    @Size(max = 50, message = "Timezone không được vượt quá 50 ký tự.")
    private String timezone;

    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username chỉ được chứa chữ cái, chữ số, dấu chấm, dấu gạch ngang, và dấu gạch dưới.")
    @Size(max = 50, message = "Username không được vượt quá 50 ký tự.")
    private String username;
}
