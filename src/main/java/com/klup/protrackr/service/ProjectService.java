package com.klup.protrackr.service;

import com.klup.protrackr.domain.Project;
import com.klup.protrackr.domain.User;
import com.klup.protrackr.domain.UserRole;
import com.klup.protrackr.dto.project.ProjectDto;
import com.klup.protrackr.dto.project.ProjectCreateRequest;
import com.klup.protrackr.dto.project.ProjectUpdateRequest;
import com.klup.protrackr.exception.ForbiddenException;
import com.klup.protrackr.exception.NotFoundException;
import com.klup.protrackr.mapper.DtoMapper;
import com.klup.protrackr.repo.ProjectRepository;
import com.klup.protrackr.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserService userService;

    public ProjectService(ProjectRepository projectRepository, UserService userService) {
        this.projectRepository = projectRepository;
        this.userService = userService;
    }

    public boolean isAdmin(UserPrincipal principal) {
        return principal.getRole() == UserRole.ADMIN;
    }

    private void assertCanAccessProject(UserPrincipal principal, Project p) {
        if (isAdmin(principal)) return;
        if (!p.getOwner().getId().equals(principal.getId())) {
            throw new ForbiddenException("You can only access your own projects");
        }
    }

    @Transactional(readOnly = true)
    public List<Project> listProjects(UserPrincipal principal) {
        if (isAdmin(principal)) {
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
        assertCanAccessProject(principal, p);
        return p;
    }

    @Transactional(readOnly = true)
    public ProjectDto getProjectDto(UserPrincipal principal, Long id) {
        return DtoMapper.projectDto(getProject(principal, id));
    }

    @Transactional
    public Project updateProject(UserPrincipal principal, Long id, ProjectUpdateRequest req) {
        Project p = getProject(principal, id);
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
        Project p = getProject(principal, id);
        projectRepository.delete(p);
    }
}
