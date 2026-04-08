package com.klup.protrackr.dto.common;

public record SimpleUserDto(
        Long id,
        String fullName,
        String email,
        String role,
        String avatarUrl
) {}

