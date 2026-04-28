package com.klup.protrackr.security;

import com.klup.protrackr.exception.ForbiddenException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class CurrentUser {
    private CurrentUser() {}

    public static UserPrincipal require() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal up)) {
            throw new ForbiddenException("Not authenticated");
        }
        return up;
    }

    public static Optional<UserPrincipal> optional() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal up)) {
            return Optional.empty();
        }
        return Optional.of(up);
    }
}
