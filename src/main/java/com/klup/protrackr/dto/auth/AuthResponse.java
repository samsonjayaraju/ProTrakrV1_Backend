package com.klup.protrackr.dto.auth;

public record AuthResponse(
        String token,
        AuthUserDto user
) {}
