package com.klup.protrackr.service;

import com.klup.protrackr.domain.PortfolioProfile;
import com.klup.protrackr.domain.Project;
import com.klup.protrackr.domain.User;
import com.klup.protrackr.domain.UserRole;
import com.klup.protrackr.dto.admin.AdminDashboardDto;
import com.klup.protrackr.dto.admin.ProjectStatusOverviewDto;
import com.klup.protrackr.dto.admin.RecentSubmissionDto;
import com.klup.protrackr.dto.admin.StudentDirectoryItemDto;
import com.klup.protrackr.dto.common.PagedResponse;
import com.klup.protrackr.exception.ForbiddenException;
import com.klup.protrackr.repo.PortfolioProfileRepository;
import com.klup.protrackr.repo.ProjectRepository;
import com.klup.protrackr.repo.ReviewRepository;
import com.klup.protrackr.repo.SkillRepository;
import com.klup.protrackr.repo.UserRepository;
import com.klup.protrackr.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {
    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_PENDING_REVIEW = "PENDING_REVIEW";
    public static final String STATUS_COMPLETED = "COMPLETED";

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ReviewRepository reviewRepository;
    private final SkillRepository skillRepository;
    private final PortfolioProfileRepository portfolioProfileRepository;

    public AdminService(UserRepository userRepository,
                        ProjectRepository projectRepository,
                        ReviewRepository reviewRepository,
                        SkillRepository skillRepository,
                        PortfolioProfileRepository portfolioProfileRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.reviewRepository = reviewRepository;
        this.skillRepository = skillRepository;
        this.portfolioProfileRepository = portfolioProfileRepository;
    }

    private void assertPrivileged(UserPrincipal principal) {
        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.FACULTY) {
            throw new ForbiddenException("Forbidden");
        }
    }

    @Transactional(readOnly = true)
    public AdminDashboardDto dashboard(UserPrincipal principal) {
        assertPrivileged(principal);

        long totalStudents = userRepository.countByRole(UserRole.STUDENT);
        long totalProjects = projectRepository.count();
        long pendingReviews = reviewRepository.countByStatusIgnoreCase(STATUS_PENDING_REVIEW);
        long completedProjects = projectRepository.countByStatusIgnoreCase(STATUS_COMPLETED);

        List<Project> recent = projectRepository.findRecent(PageRequest.of(0, 5));
        List<RecentSubmissionDto> recentSubmissions = recent.stream().map(p -> new RecentSubmissionDto(
                p.getId(),
                p.getTitle(),
                p.getOwner().getFullName(),
                p.getOwner().getAvatarUrl(),
                p.getUpdatedAt(),
                p.getStatus()
        )).toList();

        ProjectStatusOverviewDto overview = new ProjectStatusOverviewDto(
                projectRepository.countByStatusIgnoreCase(STATUS_COMPLETED),
                projectRepository.countByStatusIgnoreCase(STATUS_IN_PROGRESS),
                projectRepository.countByStatusIgnoreCase(STATUS_PENDING_REVIEW),
                projectRepository.countByStatusIgnoreCase(STATUS_DRAFT)
        );

        return new AdminDashboardDto(
                totalStudents,
                totalProjects,
                pendingReviews,
                completedProjects,
                recentSubmissions,
                overview
        );
    }

    @Transactional(readOnly = true)
    public PagedResponse<StudentDirectoryItemDto> students(UserPrincipal principal,
                                                          String search,
                                                          String department,
                                                          Integer year,
                                                          int page,
                                                          int size,
                                                          Sort sort) {
        assertPrivileged(principal);
        Page<User> result = userRepository.searchByRole(UserRole.STUDENT, emptyToNull(search), emptyToNull(department), year,
                PageRequest.of(page, size, sort));

        Map<Long, Long> projectCount = toCountMap(projectRepository.countProjectsByOwner());
        Map<Long, Long> completedCount = toCountMap(projectRepository.countProjectsByOwnerAndStatus(STATUS_COMPLETED));

        Map<Long, String> topSkill = new HashMap<>();
        for (Object[] row : skillRepository.topSkillsByUser()) {
            Long uid = ((Number) row[0]).longValue();
            if (!topSkill.containsKey(uid)) {
                topSkill.put(uid, (String) row[1]);
            }
        }

        List<Long> userIds = result.getContent().stream().map(User::getId).toList();
        Map<Long, PortfolioProfile> profiles = new HashMap<>();
        for (PortfolioProfile p : portfolioProfileRepository.findByUserIdIn(userIds)) {
            profiles.put(p.getUser().getId(), p);
        }

        List<StudentDirectoryItemDto> items = result.getContent().stream().map(u -> {
            PortfolioProfile p = profiles.get(u.getId());
            String publicUrl = null;
            if (p != null && Boolean.TRUE.equals(p.getPublicProfileEnabled()) && p.getSlug() != null && !p.getSlug().isBlank()) {
                publicUrl = "/api/portfolio/public/slug/" + p.getSlug();
            }
            return new StudentDirectoryItemDto(
                    u.getId(),
                    u.getFullName(),
                    u.getEmail(),
                    u.getRollNumber(),
                    u.getDepartment(),
                    u.getYear(),
                    u.getAvatarUrl(),
                    projectCount.getOrDefault(u.getId(), 0L),
                    completedCount.getOrDefault(u.getId(), 0L),
                    topSkill.get(u.getId()),
                    publicUrl
            );
        }).toList();

        return new PagedResponse<>(
                items,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    private static Map<Long, Long> toCountMap(List<Object[]> rows) {
        Map<Long, Long> map = new HashMap<>();
        for (Object[] r : rows) {
            map.put(((Number) r[0]).longValue(), ((Number) r[1]).longValue());
        }
        return map;
    }

    private static String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isBlank() ? null : t;
    }
}

