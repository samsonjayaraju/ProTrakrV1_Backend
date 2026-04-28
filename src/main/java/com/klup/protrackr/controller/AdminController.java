package com.klup.protrackr.controller;

import com.klup.protrackr.api.ApiResponse;
import com.klup.protrackr.dto.common.PagedResponse;
import com.klup.protrackr.security.CurrentUser;
import com.klup.protrackr.service.AdminService;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public ApiResponse<?> dashboard() {
        var principal = CurrentUser.require();
        return ApiResponse.ok(adminService.dashboard(principal));
    }

    @GetMapping("/students")
    public ApiResponse<?> students(@RequestParam(required = false) String search,
                                   @RequestParam(required = false) String department,
                                   @RequestParam(required = false) Integer year,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "20") int size,
                                   @RequestParam(defaultValue = "createdAt,desc") String sort) {
        var principal = CurrentUser.require();
        Sort s = parseSort(sort);
        PagedResponse<?> resp = adminService.students(principal, search, department, year, page, size, s);
        return ApiResponse.ok(resp);
    }

    private static Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) return Sort.by(Sort.Order.desc("createdAt"));
        String[] parts = sort.split(",", 2);
        String field = parts[0].trim();
        String dir = parts.length > 1 ? parts[1].trim().toLowerCase() : "asc";
        Sort.Direction direction = "desc".equals(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;

        // allowlist
        return switch (field) {
            case "fullName" -> Sort.by(direction, "fullName");
            case "email" -> Sort.by(direction, "email");
            case "department" -> Sort.by(direction, "department");
            case "year" -> Sort.by(direction, "year");
            case "createdAt" -> Sort.by(direction, "createdAt");
            default -> Sort.by(Sort.Order.desc("createdAt"));
        };
    }
}

