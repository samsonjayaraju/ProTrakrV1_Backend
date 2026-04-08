package com.klup.protrackr.service;

import com.klup.protrackr.domain.Project;
import com.klup.protrackr.domain.Review;
import com.klup.protrackr.domain.User;
import com.klup.protrackr.dto.review.ReviewDto;
import com.klup.protrackr.dto.review.ReviewCreateRequest;
import com.klup.protrackr.exception.BadRequestException;
import com.klup.protrackr.mapper.DtoMapper;
import com.klup.protrackr.repo.ReviewRepository;
import com.klup.protrackr.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ProjectService projectService;
    private final UserService userService;

    public ReviewService(ReviewRepository reviewRepository, ProjectService projectService, UserService userService) {
        this.reviewRepository = reviewRepository;
        this.projectService = projectService;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<Review> list(UserPrincipal principal, Long projectId) {
        if (projectId != null) {
            Project p = projectService.getProject(principal, projectId);
            return reviewRepository.findByProjectIdOrderByCreatedAtDesc(p.getId());
        }
        // Students can only see reviews for their own projects; easiest is to force projectId for students.
        if (!projectService.isAdmin(principal)) {
            throw new BadRequestException("projectId is required");
        }
        return reviewRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> listDtos(UserPrincipal principal, Long projectId) {
        return list(principal, projectId).stream().map(DtoMapper::reviewDto).toList();
    }

    @Transactional
    public Review create(UserPrincipal principal, ReviewCreateRequest req) {
        Project p = projectService.getProject(principal, req.projectId());
        User reviewer = userService.requireCurrentUserEntity(principal);
        Review r = new Review();
        r.setProject(p);
        r.setReviewer(reviewer);
        r.setTechnicalScore(req.technicalScore());
        r.setDocumentationScore(req.documentationScore());
        r.setInnovationScore(req.innovationScore());
        r.setUiUxScore(req.uiUxScore());
        r.setComments(req.comments());
        r.setStatus(req.status() == null ? "submitted" : req.status());
        r.setTotalScore(safeInt(req.technicalScore()) + safeInt(req.documentationScore()) + safeInt(req.innovationScore()) + safeInt(req.uiUxScore()));
        return reviewRepository.save(r);
    }

    @Transactional
    public ReviewDto createDto(UserPrincipal principal, ReviewCreateRequest req) {
        return DtoMapper.reviewDto(create(principal, req));
    }

    private static int safeInt(Integer v) {
        return v == null ? 0 : v;
    }
}
