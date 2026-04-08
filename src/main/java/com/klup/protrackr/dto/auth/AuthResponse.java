package com.klup.protrackr.dto.auth;

import com.klup.protrackr.dto.user.UserDto;

public record AuthResponse(
        String token,
        UserDto user
) {}

