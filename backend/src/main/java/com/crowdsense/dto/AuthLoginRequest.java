package com.crowdsense.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthLoginRequest(
        @NotBlank(message = "email is required")
        @Email(message = "email must be valid")
        @Size(max = 150, message = "email must be 150 characters or fewer")
        String email,

        @NotBlank(message = "password is required")
        @Size(max = 128, message = "password must be 128 characters or fewer")
        String password
) {
}
