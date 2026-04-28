package com.klup.protrackr.service;

import com.klup.protrackr.domain.Project;
import com.klup.protrackr.domain.User;
import com.klup.protrackr.domain.UserRole;
import com.klup.protrackr.dto.project.ProjectDetailsDto;
import com.klup.protrackr.dto.project.ProjectDto;
import com.klup.protrackr.dto.project.ProjectCreateRequest;
import com.klup.protrackr.dto.project.ProjectUpdateRequest;
import com.klup.protrackr.exception.ForbiddenException;
import com.klup.protrackr.exception.NotFoundException;
import com.klup.protrackr.mapper.DtoMapper;
import com.klup.protrackr.repo.FeedbackRepository;
import com.klup.protrackr.repo.MilestoneRepository;
import com.klup.protrackr.repo.ProjectMediaRepository;
import com.klup.protrackr.repo.ProjectRepository;
import com.klup.protrackr.repo.ReviewRepository;
import com.klup.protrackr.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserService userService;
    private final MilestoneRepository milestoneRepository;
    private final FeedbackRepository feedbackRepository;
    private final ReviewRepository reviewRepository;
    private final ProjectMediaRepository projectMediaRepository;

    public ProjectService(ProjectRepository projectRepository,
                          UserService userService,
                          MilestoneRepository milestoneRepository,
                          FeedbackRepository feedbackRepository,
                          ReviewRepository reviewRepository,
                          ProjectMediaRepository projectMediaRepository) {
        this.projectRepository = projectRepository;
        this.userService = userService;
        this.milestoneRepository = milestoneRepository;
        this.feedbackRepository = feedbackRepository;
        this.reviewRepository = reviewRepository;
        this.projectMediaRepository = projectMediaRepository;
    }

    public boolean isAdmin(UserPrincipal principal) {
        return principal.getRole() == UserRole.ADMIN;
    }

    public boolean isPrivileged(UserPrincipal principal) {
        return principal.getRole() == UserRole.ADMIN || principal.getRole() == UserRole.FACULTY;
    }

    private void assertCanViewProject(UserPrincipal principal, Project p) {
        if (isPrivileged(principal)) return;
        if (!p.getOwner().getId().equals(principal.getId())) throw new ForbiddenException("Forbidden");
    }

    private void assertCanModifyProject(UserPrincipal principal, Project p) {
        if (isAdmin(principal)) return;
        if (!p.getOwner().getId().equals(principal.getId())) throw new ForbiddenException("Forbidden");
    }

    private void assertCanAccessProjectDetails(UserPrincipal principal, Project p) {
        if (isAdmin(principal) || principal.getRole() == UserRole.FACULTY) return;
        if (p.getOwner().getId().equals(principal.getId())) return;
        if (projectRepository.isUserInTeam(p.getId(), principal.getId()) > 0) return;
        throw new ForbiddenException("Forbidden");
    }

    @Transactional(readOnly = true)
    public Project getProjectForTeamAccess(UserPrincipal principal, Long id) {
        Project p = projectRepository.findById(id).orElseThrow(() -> new NotFoundException("Project not found"));
        assertCanAccessProjectDetails(principal, p);
        return p;
    }

    @Transactional(readOnly = true)
    public List<Project> listProjects(UserPrincipal principal) {
        if (isPrivileged(principal)) {
            return projectRepository.findAllOrderByUpdatedAtDesc();
        }
        return projectRepository.findByOwnerIdOrderByUpdatedAtDesc(principal.getId());
    }

    @Transactional(readOnly = true)
    public List<ProjectDto> listProjectDtos(UserPrincipal principal) {
        return listProjects(principal).stream().map(DtoMapper::projectDto).toList();
    }

    @Transactional
    public Project createProject(UserPrincipal principal, ProjectCreateRequest req) {
        User owner = userService.requireCurrentUserEntity(principal);
        Project p = new Project();
        p.setOwner(owner);
        p.setTitle(req.title());
        p.setDescription(req.description());
        p.setCategory(req.category());
        p.setStatus(req.status());
        p.setProgress(req.progress());
        p.setDueDate(req.dueDate());
        p.setTechStack(req.techStack());
        p.setSourceUrl(req.sourceUrl());
        p.setDemoUrl(req.demoUrl());
        return projectRepository.save(p);
    }

    @Transactional
    public ProjectDto createProjectDto(UserPrincipal principal, ProjectCreateRequest req) {
        return DtoMapper.projectDto(createProject(principal, req));
    }

    @Transactional(readOnly = true)
    public Project getProject(UserPrincipal principal, Long id) {
        Project p = projectRepository.findById(id).orElseThrow(() -> new NotFoundException("Project not found"));
        assertCanViewProject(principal, p);
        return p;
    }

    @Transactional(readOnly = true)
    public ProjectDto getProjectDto(UserPrincipal principal, Long id) {
        return DtoMapper.projectDto(getProject(principal, id));
    }

    @Transactional(readOnly = true)
    public ProjectDetailsDto getProjectDetails(UserPrincipal principal, Long id) {
        Project p = getProjectForTeamAccess(principal, id);

        var milestones = milestoneRepository.findByProjectIdOrdered(p.getId()).stream().map(DtoMapper::milestoneDto).toList();
        var feedback = feedbackRepository.findByProjectIdOrderByCreatedAtDesc(p.getId()).stream().map(DtoMapper::feedbackDto).toList();
        var reviews = reviewRepository.findByProjectIdOrderByCreatedAtDesc(p.getId()).stream().map(DtoMapper::reviewDto).toList();
        var media = projectMediaRepository.findByProjectIdOrderByCreatedAtDesc(p.getId()).stream().map(DtoMapper::projectMediaDto).toList();

        Set<com.klup.protrackr.domain.User> teamMembers = p.getTeamMembers();
        java.util.LinkedHashMap<Long, com.klup.protrackr.domain.User> unique = new java.util.LinkedHashMap<>();
        unique.put(p.getOwner().getId(), p.getOwner());
        for (var tm : teamMembers) unique.put(tm.getId(), tm);
        var team = unique.values().stream().map(DtoMapper::simpleUser).toList();

        return new ProjectDetailsDto(
                DtoMapper.projectDto(p),
                milestones,
                feedback,
                reviews,
                team,
                media
        );
    }

    @Transactional
    public Project updateProject(UserPrincipal principal, Long id, ProjectUpdateRequest req) {
        Project p = projectRepository.findById(id).orElseThrow(() -> new NotFoundException("Project not found"));
        assertCanModifyProject(principal, p);
        if (req.title() != null) p.setTitle(req.title());
        if (req.description() != null) p.setDescription(req.description());
        if (req.category() != null) p.setCategory(req.category());
        if (req.status() != null) p.setStatus(req.status());
        if (req.progress() != null) p.setProgress(req.progress());
        if (req.dueDate() != null) p.setDueDate(req.dueDate());
        if (req.techStack() != null) p.setTechStack(req.techStack());
        if (req.sourceUrl() != null) p.setSourceUrl(req.sourceUrl());
        if (req.demoUrl() != null) p.setDemoUrl(req.demoUrl());
        return projectRepository.save(p);
    }

    @Transactional
    public ProjectDto updateProjectDto(UserPrincipal principal, Long id, ProjectUpdateRequest req) {
        return DtoMapper.projectDto(updateProject(principal, id, req));
    }

    @Transactional
    public void deleteProject(UserPrincipal principal, Long id) {
        Project p = projectRepository.findById(id).orElseThrow(() -> new NotFoundException("Project not found"));
        assertCanModifyProject(principal, p);
        projectRepository.delete(p);
    }

    @Transactional
    public ProjectDto updateReviewStatus(UserPrincipal principal, Long id, String status) {
        if (!isPrivileged(principal)) {
            throw new ForbiddenException("Forbidden");
        }
        Project p = projectRepository.findById(id).orElseThrow(() -> new NotFoundException("Project not found"));
        p.setStatus(status);
        return DtoMapper.projectDto(projectRepository.save(p));
    }
}
