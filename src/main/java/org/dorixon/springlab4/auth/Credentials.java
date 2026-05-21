package org.dorixon.springlab4.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record Credentials(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotNull(message = "Password cannot be null")
        @Size(min = 8, max = 25, message = "Password must be between 8 and 25 characters")
        String password
) {}
