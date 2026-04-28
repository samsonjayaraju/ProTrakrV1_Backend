package com.klup.protrackr.dto.auth;

public record AuthUserDto(
        Long id,
        String fullName,
        String email,
        String role
) {}

