package com.klup.protrackr.service;

import com.klup.protrackr.domain.UserRole;
import com.klup.protrackr.exception.ForbiddenException;
import com.klup.protrackr.repo.FeedbackRepository;
import com.klup.protrackr.repo.MilestoneRepository;
import com.klup.protrackr.repo.ProjectRepository;
import com.klup.protrackr.repo.ReviewRepository;
import com.klup.protrackr.repo.UserRepository;
import com.klup.protrackr.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class StatsService {
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final MilestoneRepository milestoneRepository;
    private final FeedbackRepository feedbackRepository;
    private final ReviewRepository reviewRepository;

    public StatsService(UserRepository userRepository,
                        ProjectRepository projectRepository,
                        MilestoneRepository milestoneRepository,
                        FeedbackRepository feedbackRepository,
                        ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.milestoneRepository = milestoneRepository;
        this.feedbackRepository = feedbackRepository;
        this.reviewRepository = reviewRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> studentStats(UserPrincipal principal) {
        long projects = projectRepository.countByOwnerId(principal.getId());
        long milestones = milestoneRepository.countByProjectOwnerId(principal.getId());
        long milestonesDone = milestoneRepository.countByProjectOwnerIdAndCompletedTrue(principal.getId());
        long feedback = feedbackRepository.countByProjectOwnerId(principal.getId());

        return Map.of(
                "projects", projects,
                "milestones", milestones,
                "milestonesCompleted", milestonesDone,
                "feedbackCount", feedback
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> adminStats(UserPrincipal principal) {
        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.FACULTY) {
            throw new ForbiddenException("Admin only");
        }
        long users = userRepository.count();
        long projects = projectRepository.count();
        long reviews = reviewRepository.count();
        long pendingReviews = reviewRepository.countByStatusIgnoreCase(AdminService.STATUS_PENDING_REVIEW);

        return Map.of(
                "users", users,
                "projects", projects,
                "reviews", reviews,
                "pendingReviews", pendingReviews
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> reports(UserPrincipal principal) {
        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.FACULTY) {
            throw new ForbiddenException("Admin only");
        }
        return Map.of(
                "totals", adminStats(principal),
                "generatedAt", java.time.Instant.now().toString()
        );
    }
}
