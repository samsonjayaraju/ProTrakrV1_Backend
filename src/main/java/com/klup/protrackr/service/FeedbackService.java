package com.klup.protrackr.service;

import com.klup.protrackr.domain.Feedback;
import com.klup.protrackr.domain.Project;
import com.klup.protrackr.domain.User;
import com.klup.protrackr.domain.UserRole;
import com.klup.protrackr.dto.feedback.FeedbackDto;
import com.klup.protrackr.dto.feedback.FeedbackCreateRequest;
import com.klup.protrackr.dto.feedback.FeedbackUpdateRequest;
import com.klup.protrackr.exception.ForbiddenException;
import com.klup.protrackr.exception.NotFoundException;
import com.klup.protrackr.mapper.DtoMapper;
import com.klup.protrackr.repo.FeedbackRepository;
import com.klup.protrackr.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final ProjectService projectService;
    private final UserService userService;

    public FeedbackService(FeedbackRepository feedbackRepository, ProjectService projectService, UserService userService) {
        this.feedbackRepository = feedbackRepository;
        this.projectService = projectService;
        this.userService = userService;
    }

    private boolean isAdmin(UserPrincipal principal) {
        return principal.getRole() == UserRole.ADMIN || principal.getRole() == UserRole.FACULTY;
    }

    @Transactional(readOnly = true)
    public List<Feedback> listByProject(UserPrincipal principal, Long projectId) {
        Project p = projectService.getProject(principal, projectId);
        return feedbackRepository.findByProjectIdOrderByCreatedAtDesc(p.getId());
    }

    @Transactional(readOnly = true)
    public List<FeedbackDto> listByProjectDtos(UserPrincipal principal, Long projectId) {
        return listByProject(principal, projectId).stream().map(DtoMapper::feedbackDto).toList();
    }

    @Transactional
    public Feedback create(UserPrincipal principal, FeedbackCreateRequest req) {
        Project p = projectService.getProject(principal, req.projectId());
        User author = userService.requireCurrentUserEntity(principal);
        Feedback f = new Feedback();
        f.setProject(p);
        f.setAuthor(author);
        f.setText(req.text());
        return feedbackRepository.save(f);
    }

    @Transactional
    public FeedbackDto createDto(UserPrincipal principal, FeedbackCreateRequest req) {
        return DtoMapper.feedbackDto(create(principal, req));
    }

    @Transactional(readOnly = true)
    public Feedback get(UserPrincipal principal, Long id) {
        Feedback f = feedbackRepository.findById(id).orElseThrow(() -> new NotFoundException("Feedback not found"));
        projectService.getProject(principal, f.getProject().getId());
        return f;
    }

    @Transactional(readOnly = true)
    public FeedbackDto getDto(UserPrincipal principal, Long id) {
        return DtoMapper.feedbackDto(get(principal, id));
    }

    @Transactional
    public Feedback update(UserPrincipal principal, Long id, FeedbackUpdateRequest req) {
        Feedback f = get(principal, id);
        if (!isAdmin(principal) && !f.getAuthor().getId().equals(principal.getId())) {
            throw new ForbiddenException("You can only edit your own feedback");
        }
        f.setText(req.text());
        return feedbackRepository.save(f);
    }

    @Transactional
    public FeedbackDto updateDto(UserPrincipal principal, Long id, FeedbackUpdateRequest req) {
        return DtoMapper.feedbackDto(update(principal, id, req));
    }

    @Transactional
    public void delete(UserPrincipal principal, Long id) {
        Feedback f = get(principal, id);
        if (!isAdmin(principal) && !f.getAuthor().getId().equals(principal.getId())) {
            throw new ForbiddenException("You can only delete your own feedback");
        }
        feedbackRepository.delete(f);
    }
}
