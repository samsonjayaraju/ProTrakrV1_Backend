package com.klup.protrackr.service;

import com.klup.protrackr.domain.User;
import com.klup.protrackr.domain.UserPreferences;
import com.klup.protrackr.dto.user.ChangePasswordRequest;
import com.klup.protrackr.dto.user.UpdatePreferencesRequest;
import com.klup.protrackr.dto.user.UpdateProfileRequest;
import com.klup.protrackr.dto.user.UserPreferencesDto;
import com.klup.protrackr.exception.BadRequestException;
import com.klup.protrackr.exception.ForbiddenException;
import com.klup.protrackr.exception.NotFoundException;
import com.klup.protrackr.repo.UserRepository;
import com.klup.protrackr.repo.UserPreferencesRepository;
import com.klup.protrackr.security.UserPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserPreferencesRepository userPreferencesRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserPreferencesRepository userPreferencesRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userPreferencesRepository = userPreferencesRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public User requireUserEntity(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public User requireCurrentUserEntity(UserPrincipal principal) {
        return requireUserEntity(principal.getId());
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User updateProfile(UserPrincipal principal, UpdateProfileRequest req) {
        User u = requireCurrentUserEntity(principal);
        if (req.fullName() != null) {
            u.setFullName(req.fullName());
        } else if (req.firstName() != null || req.lastName() != null) {
            String fn = req.firstName() == null ? "" : req.firstName().trim();
            String ln = req.lastName() == null ? "" : req.lastName().trim();
            String merged = (fn + " " + ln).trim().replaceAll("\\s+", " ");
            if (!merged.isBlank()) u.setFullName(merged);
        }
        if (req.department() != null) u.setDepartment(req.department());
        if (req.year() != null) u.setYear(req.year());
        if (req.rollNumber() != null) u.setRollNumber(req.rollNumber());
        if (req.bio() != null) u.setBio(req.bio());
        if (req.location() != null) u.setLocation(req.location());
        if (req.githubUrl() != null) u.setGithubUrl(req.githubUrl());
        if (req.portfolioPublic() != null) u.setPortfolioPublic(req.portfolioPublic());
        return userRepository.save(u);
    }

    @Transactional
    public void changePassword(UserPrincipal principal, ChangePasswordRequest req) {
        User u = requireCurrentUserEntity(principal);
        if (!passwordEncoder.matches(req.currentPassword(), u.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }
        if (passwordEncoder.matches(req.newPassword(), u.getPasswordHash())) {
            throw new BadRequestException("New password must be different");
        }
        u.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(u);
    }

    @Transactional(readOnly = true)
    public User getUserByIdWithAuth(UserPrincipal principal, Long userId, boolean isAdmin) {
        if (!isAdmin && !principal.getId().equals(userId)) {
            throw new ForbiddenException("Forbidden");
        }
        return requireUserEntity(userId);
    }

    @Transactional
    public UserPreferencesDto getPreferences(UserPrincipal principal) {
        User u = requireCurrentUserEntity(principal);
        UserPreferences p = userPreferencesRepository.findById(u.getId())
                .orElseGet(() -> {
                    UserPreferences created = new UserPreferences();
                    created.setUserId(u.getId());
                    return userPreferencesRepository.save(created);
                });
        return new UserPreferencesDto(
                p.getProjectUpdatesEmail(),
                p.getMilestoneRemindersEmail(),
                p.getPlatformAnnouncementsEmail(),
                u.getPortfolioPublic()
        );
    }

    @Transactional
    public UserPreferencesDto updatePreferences(UserPrincipal principal, UpdatePreferencesRequest req) {
        User u = requireCurrentUserEntity(principal);
        UserPreferences p = userPreferencesRepository.findById(u.getId())
                .orElseGet(() -> {
                    UserPreferences created = new UserPreferences();
                    created.setUserId(u.getId());
                    return created;
                });
        if (req.projectUpdatesEmail() != null) p.setProjectUpdatesEmail(req.projectUpdatesEmail());
        if (req.milestoneRemindersEmail() != null) p.setMilestoneRemindersEmail(req.milestoneRemindersEmail());
        if (req.platformAnnouncementsEmail() != null) p.setPlatformAnnouncementsEmail(req.platformAnnouncementsEmail());
        userPreferencesRepository.save(p);

        if (req.publicPortfolioEnabled() != null) {
            u.setPortfolioPublic(req.publicPortfolioEnabled());
            userRepository.save(u);
        }
        return getPreferences(principal);
    }
}
