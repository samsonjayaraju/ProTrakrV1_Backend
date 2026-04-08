package com.klup.protrackr.controller;

import com.klup.protrackr.api.ApiResponse;
import com.klup.protrackr.dto.project.ProjectCreateRequest;
import com.klup.protrackr.dto.project.ProjectUpdateRequest;
import com.klup.protrackr.security.CurrentUser;
import com.klup.protrackr.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ApiResponse<?> list() {
        var principal = CurrentUser.require();
        return ApiResponse.ok(projectService.listProjectDtos(principal));
    }

    @PostMapping
    public ApiResponse<?> create(@Valid @RequestBody ProjectCreateRequest req) {
        var principal = CurrentUser.require();
        return ApiResponse.ok(projectService.createProjectDto(principal, req));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> get(@PathVariable Long id) {
        var principal = CurrentUser.require();
        return ApiResponse.ok(projectService.getProjectDto(principal, id));
    }

    @PutMapping("/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @Valid @RequestBody ProjectUpdateRequest req) {
        var principal = CurrentUser.require();
        return ApiResponse.ok(projectService.updateProjectDto(principal, id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        var principal = CurrentUser.require();
        projectService.deleteProject(principal, id);
        return ApiResponse.ok(null, "Deleted");
    }
}
