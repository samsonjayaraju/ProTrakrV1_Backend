package com.klup.protrackr.controller;

import com.klup.protrackr.api.ApiResponse;
import com.klup.protrackr.security.CurrentUser;
import com.klup.protrackr.service.StatsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {
    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/student")
    public ApiResponse<?> student() {
        var principal = CurrentUser.require();
        return ApiResponse.ok(statsService.studentStats(principal));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    public ApiResponse<?> admin() {
        var principal = CurrentUser.require();
        return ApiResponse.ok(statsService.adminStats(principal));
    }

    @GetMapping("/reports")
    @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    public ApiResponse<?> reports() {
        var principal = CurrentUser.require();
        return ApiResponse.ok(statsService.reports(principal));
    }
}
