package com.klup.protrackr.mapper;

import com.klup.protrackr.domain.User;
import com.klup.protrackr.dto.auth.AuthUserDto;

public final class AuthMapper {
    private AuthMapper() {}

    public static AuthUserDto authUser(User u) {
        return new AuthUserDto(u.getId(), u.getFullName(), u.getEmail(), u.getRole().dbValue());
    }
}

