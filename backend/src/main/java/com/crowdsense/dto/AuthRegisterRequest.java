package com.crowdsense.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRegisterRequest(
        @NotBlank(message = "name is required")
        @Size(max = 120, message = "name must be 120 characters or fewer")
        String name,

        @NotBlank(message = "email is required")
        @Email(message = "email must be valid")
        @Size(max = 150, message = "email must be 150 characters or fewer")
        String email,

        @NotBlank(message = "password is required")
        @Size(min = 8, max = 128, message = "password must be between 8 and 128 characters")
        String password
) {
}
