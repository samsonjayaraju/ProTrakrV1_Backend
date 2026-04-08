package com.klup.protrackr.dto.user;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @JsonAlias({"full_name", "name", "fullname"})
        @Size(max = 255) String fullName,
        @Size(max = 100) String department,
        Integer year,
        @JsonAlias({"roll_number"})
        @Size(max = 50) String rollNumber,
        String bio,
        @Size(max = 255) String location,
        @Size(max = 500) String githubUrl,
        @JsonAlias({"portfolio_public"})
        Boolean portfolioPublic
) {}
