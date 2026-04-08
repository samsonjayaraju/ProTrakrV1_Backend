package com.klup.protrackr.controller;

import com.klup.protrackr.api.ApiResponse;
import com.klup.protrackr.dto.milestone.MilestoneCreateRequest;
import com.klup.protrackr.dto.milestone.MilestoneUpdateRequest;
import com.klup.protrackr.security.CurrentUser;
import com.klup.protrackr.service.MilestoneService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/milestones")
public class MilestoneController {
    private final MilestoneService milestoneService;

    public MilestoneController(MilestoneService milestoneService) {
        this.milestoneService = milestoneService;
    }

    @GetMapping
    public ApiResponse<?> list(@RequestParam Long projectId) {
        var principal = CurrentUser.require();
        return ApiResponse.ok(milestoneService.listByProjectDtos(principal, projectId));
    }

    @PostMapping
    public ApiResponse<?> create(@Valid @RequestBody MilestoneCreateRequest req) {
        var principal = CurrentUser.require();
        return ApiResponse.ok(milestoneService.createDto(principal, req));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> get(@PathVariable Long id) {
        var principal = CurrentUser.require();
        return ApiResponse.ok(milestoneService.getDto(principal, id));
    }

    @PutMapping("/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @Valid @RequestBody MilestoneUpdateRequest req) {
        var principal = CurrentUser.require();
        return ApiResponse.ok(milestoneService.updateDto(principal, id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        var principal = CurrentUser.require();
        milestoneService.delete(principal, id);
        return ApiResponse.ok(null, "Deleted");
    }
}
