package com.klup.protrackr.service;

import com.klup.protrackr.domain.User;
import com.klup.protrackr.domain.UserRole;
import com.klup.protrackr.dto.auth.AuthResponse;
import com.klup.protrackr.dto.auth.LoginRequest;
import com.klup.protrackr.dto.auth.RegisterRequest;
import com.klup.protrackr.exception.BadRequestException;
import com.klup.protrackr.mapper.AuthMapper;
import com.klup.protrackr.repo.UserRepository;
import com.klup.protrackr.security.JwtService;
import com.klup.protrackr.security.UserPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmailIgnoreCase(req.email())) {
            throw new BadRequestException("Email already registered");
        }
        User u = new User();
        u.setFullName(req.fullName());
        u.setEmail(req.email().trim().toLowerCase());
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        u.setRole(parseRole(req.role()));
        u.setDepartment(req.department());
        u.setYear(req.year());
        u.setRollNumber(req.rollNumber());
        User saved = userRepository.save(u);

        UserPrincipal principal = UserPrincipal.fromUser(saved);
        String token = jwtService.generateToken(principal);
        return new AuthResponse(token, AuthMapper.authUser(saved));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        User u = userRepository.findByEmailIgnoreCase(req.email())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));
        if (!passwordEncoder.matches(req.password(), u.getPasswordHash())) {
            throw new BadRequestException("Invalid email or password");
        }
        UserPrincipal principal = UserPrincipal.fromUser(u);
        String token = jwtService.generateToken(principal);
        return new AuthResponse(token, AuthMapper.authUser(u));
    }

    private static UserRole parseRole(String role) {
        if (role == null || role.isBlank()) return UserRole.STUDENT;
        String r = role.trim().toLowerCase(Locale.ROOT);
        return switch (r) {
            case "student" -> UserRole.STUDENT;
            case "faculty" -> UserRole.FACULTY;
            case "admin" -> UserRole.ADMIN;
            default -> throw new BadRequestException("Invalid role: " + role);
        };
    }
}
