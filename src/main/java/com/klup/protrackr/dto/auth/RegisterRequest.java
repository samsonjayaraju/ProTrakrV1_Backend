package com.klup.protrackr.dto.auth;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @JsonAlias({"full_name", "name", "fullname"})
        @NotBlank @Size(max = 255) String fullName,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 6, max = 100) String password,
        String department,
        @JsonAlias({"academic_year"})
        Integer year,
        @JsonAlias({"roll_number"})
        String rollNumber
) {}
