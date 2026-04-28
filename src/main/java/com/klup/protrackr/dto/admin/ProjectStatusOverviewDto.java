package com.klup.protrackr.dto.admin;

public record ProjectStatusOverviewDto(
        long completed,
        long inProgress,
        long pendingReview,
        long draft
) {}

