package com.klup.protrackr.controller;

import com.klup.protrackr.api.ApiResponse;
import com.klup.protrackr.dto.auth.AuthResponse;
import com.klup.protrackr.dto.auth.LoginRequest;
import com.klup.protrackr.dto.auth.RegisterRequest;
import com.klup.protrackr.mapper.DtoMapper;
import com.klup.protrackr.security.CurrentUser;
import com.klup.protrackr.service.AuthService;
import com.klup.protrackr.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ApiResponse.ok(authService.register(req));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok(authService.login(req));
    }

    @GetMapping("/me")
    public ApiResponse<?> me() {
        var principal = CurrentUser.require();
        var user = userService.requireCurrentUserEntity(principal);
        return ApiResponse.ok(DtoMapper.userDto(user));
    }
}

