package com.klup.protrackr.dto.admin;

import java.util.List;

public record AdminDashboardDto(
        long totalStudents,
        long totalProjects,
        long pendingReviews,
        long completedProjects,
        List<RecentSubmissionDto> recentSubmissions,
        ProjectStatusOverviewDto projectStatusOverview
) {}

