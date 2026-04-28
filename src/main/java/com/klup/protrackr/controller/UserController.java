package com.klup.protrackr.controller;

import com.klup.protrackr.api.ApiResponse;
import com.klup.protrackr.domain.UserRole;
import com.klup.protrackr.dto.user.ChangePasswordRequest;
import com.klup.protrackr.dto.user.UpdatePreferencesRequest;
import com.klup.protrackr.dto.user.UpdateProfileRequest;
import com.klup.protrackr.mapper.DtoMapper;
import com.klup.protrackr.security.CurrentUser;
import com.klup.protrackr.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ApiResponse<?> getProfile() {
        var principal = CurrentUser.require();
        var user = userService.requireCurrentUserEntity(principal);
        return ApiResponse.ok(DtoMapper.userDto(user));
    }

    @PutMapping("/profile")
    public ApiResponse<?> updateProfile(@Valid @RequestBody UpdateProfileRequest req) {
        var principal = CurrentUser.require();
        var updated = userService.updateProfile(principal, req);
        return ApiResponse.ok(DtoMapper.userDto(updated), "Profile updated");
    }

    @PutMapping("/password")
    public ApiResponse<?> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        var principal = CurrentUser.require();
        userService.changePassword(principal, req);
        return ApiResponse.ok(null, "Password updated");
    }

    @GetMapping("/preferences")
    public ApiResponse<?> getPreferences() {
        var principal = CurrentUser.require();
        return ApiResponse.ok(userService.getPreferences(principal));
    }

    @PutMapping("/preferences")
    public ApiResponse<?> updatePreferences(@RequestBody UpdatePreferencesRequest req) {
        var principal = CurrentUser.require();
        return ApiResponse.ok(userService.updatePreferences(principal, req), "Preferences updated");
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    public ApiResponse<?> allUsers() {
        return ApiResponse.ok(userService.getAllUsers().stream().map(DtoMapper::userDto).toList());
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getUser(@PathVariable Long id) {
        var principal = CurrentUser.require();
        boolean privileged = principal.getRole() == UserRole.ADMIN || principal.getRole() == UserRole.FACULTY;
        var user = userService.getUserByIdWithAuth(principal, id, privileged);
        return ApiResponse.ok(DtoMapper.userDto(user));
    }
}
