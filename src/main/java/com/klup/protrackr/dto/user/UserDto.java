package com.klup.protrackr.dto.user;

import java.time.Instant;

public record UserDto(
        Long id,
        String fullName,
        String email,
        String role,
        String department,
        Integer year,
        String rollNumber,
        String bio,
        String location,
        String avatarUrl,
        String githubUrl,
        Boolean portfolioPublic,
        Instant createdAt,
        Instant updatedAt
) {}

