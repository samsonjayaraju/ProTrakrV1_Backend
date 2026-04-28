package com.klup.protrackr.service;

import com.klup.protrackr.domain.Achievement;
import com.klup.protrackr.domain.HighlightedProject;
import com.klup.protrackr.domain.PortfolioProfile;
import com.klup.protrackr.domain.Project;
import com.klup.protrackr.domain.Skill;
import com.klup.protrackr.domain.User;
import com.klup.protrackr.domain.UserRole;
import com.klup.protrackr.dto.portfolio.AchievementDto;
import com.klup.protrackr.dto.portfolio.PortfolioDto;
import com.klup.protrackr.dto.portfolio.PortfolioUpdateRequest;
import com.klup.protrackr.dto.portfolio.SkillDto;
import com.klup.protrackr.exception.BadRequestException;
import com.klup.protrackr.exception.ForbiddenException;
import com.klup.protrackr.exception.NotFoundException;
import com.klup.protrackr.mapper.DtoMapper;
import com.klup.protrackr.repo.AchievementRepository;
import com.klup.protrackr.repo.HighlightedProjectRepository;
import com.klup.protrackr.repo.PortfolioProfileRepository;
import com.klup.protrackr.repo.ProjectRepository;
import com.klup.protrackr.repo.SkillRepository;
import com.klup.protrackr.repo.UserRepository;
import com.klup.protrackr.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Service
public class PortfolioService {
    private final UserRepository userRepository;
    private final PortfolioProfileRepository profileRepository;
    private final SkillRepository skillRepository;
    private final AchievementRepository achievementRepository;
    private final HighlightedProjectRepository highlightedProjectRepository;
    private final ProjectRepository projectRepository;

    public PortfolioService(UserRepository userRepository,
                            PortfolioProfileRepository profileRepository,
                            SkillRepository skillRepository,
                            AchievementRepository achievementRepository,
                            HighlightedProjectRepository highlightedProjectRepository,
                            ProjectRepository projectRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.skillRepository = skillRepository;
        this.achievementRepository = achievementRepository;
        this.highlightedProjectRepository = highlightedProjectRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional
    public PortfolioDto getMyPortfolio(UserPrincipal principal) {
        User u = requireUser(principal.getId());
        PortfolioProfile profile = getOrCreateProfile(u);
        return toDto(u, profile);
    }

    @Transactional
    public PortfolioDto updateMyPortfolio(UserPrincipal principal, PortfolioUpdateRequest req) {
        User u = requireUser(principal.getId());
        PortfolioProfile profile = getOrCreateProfile(u);

        if (req.fullName() != null) u.setFullName(req.fullName());
        if (req.email() != null) u.setEmail(req.email().trim().toLowerCase(Locale.ROOT));
        if (req.bio() != null) u.setBio(req.bio());
        if (req.location() != null) u.setLocation(req.location());
        if (req.githubUrl() != null) u.setGithubUrl(req.githubUrl());
        userRepository.save(u);

        if (req.headline() != null) profile.setHeadline(req.headline());
        if (req.linkedinUrl() != null) profile.setLinkedinUrl(req.linkedinUrl());
        if (req.websiteUrl() != null) profile.setWebsiteUrl(req.websiteUrl());
        if (req.publicProfileEnabled() != null) {
            profile.setPublicProfileEnabled(req.publicProfileEnabled());
            u.setPortfolioPublic(req.publicProfileEnabled());
            userRepository.save(u);
        }
        if (req.slug() != null) {
            String slug = normalizeSlug(req.slug());
            if (profileRepository.existsBySlugIgnoreCaseAndUserIdNot(slug, u.getId())) {
                throw new BadRequestException("Slug already taken");
            }
            profile.setSlug(slug);
        } else if ((profile.getSlug() == null || profile.getSlug().isBlank()) && u.getFullName() != null) {
            // best-effort default slug
            String slug = normalizeSlug(u.getFullName());
            if (!slug.isBlank() && !profileRepository.existsBySlugIgnoreCaseAndUserIdNot(slug, u.getId())) {
                profile.setSlug(slug);
            }
        }
        profileRepository.save(profile);

        if (req.skills() != null) {
            skillRepository.deleteByUserId(u.getId());
            List<Skill> toSave = new ArrayList<>();
            for (SkillDto s : req.skills()) {
                if (s == null || s.name() == null || s.name().isBlank()) continue;
                Skill sk = new Skill();
                sk.setUser(u);
                sk.setName(s.name().trim());
                sk.setCategory(s.category());
                toSave.add(sk);
            }
            skillRepository.saveAll(toSave);
        }

        if (req.achievements() != null) {
            achievementRepository.deleteByUserId(u.getId());
            List<Achievement> toSave = new ArrayList<>();
            for (AchievementDto a : req.achievements()) {
                if (a == null || a.title() == null || a.title().isBlank()) continue;
                Achievement ach = new Achievement();
                ach.setUser(u);
                ach.setTitle(a.title().trim());
                ach.setIssuer(a.issuer());
                ach.setDate(a.date());
                ach.setDescription(a.description());
                toSave.add(ach);
            }
            achievementRepository.saveAll(toSave);
        }

        if (req.highlightedProjectIds() != null) {
            highlightedProjectRepository.deleteByUserId(u.getId());
            int i = 0;
            for (Long pid : req.highlightedProjectIds()) {
                if (pid == null) continue;
                Project p = projectRepository.findById(pid).orElse(null);
                if (p == null || !Objects.equals(p.getOwner().getId(), u.getId())) continue;
                HighlightedProject hp = new HighlightedProject();
                hp.setUser(u);
                hp.setProject(p);
                hp.setSortOrder(i++);
                highlightedProjectRepository.save(hp);
            }
        }

        return toDto(u, profile);
    }

    @Transactional(readOnly = true)
    public PortfolioDto getPublicPortfolioByUserId(Optional<UserPrincipal> principal, Long userId) {
        User u = requireUser(userId);
        PortfolioProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Portfolio not found"));

        boolean privileged = principal.map(p -> p.getRole() == UserRole.ADMIN || p.getRole() == UserRole.FACULTY).orElse(false);
        if (!privileged && !Boolean.TRUE.equals(profile.getPublicProfileEnabled())) {
            throw new NotFoundException("Portfolio not public");
        }
        return toDto(u, profile);
    }

    @Transactional(readOnly = true)
    public PortfolioDto getPublicPortfolioBySlug(Optional<UserPrincipal> principal, String slug) {
        PortfolioProfile profile = profileRepository.findBySlugIgnoreCase(slug)
                .orElseThrow(() -> new NotFoundException("Portfolio not found"));
        return getPublicPortfolioByUserId(principal, profile.getUser().getId());
    }

    @Transactional(readOnly = true)
    public PortfolioDto getPortfolioForAdmin(UserPrincipal principal, Long userId) {
        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.FACULTY) {
            throw new ForbiddenException("Forbidden");
        }
        return getPublicPortfolioByUserId(Optional.of(principal), userId);
    }

    @Transactional
    public void setResumeUrl(UserPrincipal principal, String resumeUrl) {
        User u = requireUser(principal.getId());
        PortfolioProfile profile = getOrCreateProfile(u);
        profile.setResumeUrl(resumeUrl);
        profileRepository.save(profile);
    }

    private PortfolioProfile getOrCreateProfile(User u) {
        return profileRepository.findByUserId(u.getId())
                .orElseGet(() -> {
                    PortfolioProfile p = new PortfolioProfile();
                    p.setUser(u);
                    p.setPublicProfileEnabled(Boolean.TRUE.equals(u.getPortfolioPublic()));
                    if (u.getFullName() != null) p.setSlug(normalizeSlug(u.getFullName()));
                    return profileRepository.save(p);
                });
    }

    private PortfolioDto toDto(User u, PortfolioProfile profile) {
        List<SkillDto> skills = skillRepository.findByUserIdOrderByIdAsc(u.getId()).stream()
                .map(s -> new SkillDto(s.getName(), s.getCategory()))
                .toList();
        List<AchievementDto> achievements = achievementRepository.findByUserIdOrderByDateDescIdDesc(u.getId()).stream()
                .map(a -> new AchievementDto(a.getTitle(), a.getIssuer(), a.getDate(), a.getDescription()))
                .toList();
        List<Project> highlighted = highlightedProjectRepository.findByUserIdOrderBySortOrderAscIdAsc(u.getId())
                .stream()
                .map(HighlightedProject::getProject)
                .toList();
        return new PortfolioDto(
                u.getFullName(),
                profile.getHeadline(),
                u.getBio(),
                u.getLocation(),
                u.getEmail(),
                u.getGithubUrl(),
                profile.getLinkedinUrl(),
                profile.getWebsiteUrl(),
                u.getAvatarUrl(),
                profile.getResumeUrl(),
                profile.getPublicProfileEnabled(),
                profile.getSlug(),
                skills,
                achievements,
                highlighted.stream().map(DtoMapper::projectDto).toList()
        );
    }

    private User requireUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    }

    private static String normalizeSlug(String raw) {
        String s = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
        s = s.replaceAll("[^a-z0-9]+", "-");
        s = s.replaceAll("^-+|-+$", "");
        if (s.length() > 64) s = s.substring(0, 64);
        return s;
    }
}

