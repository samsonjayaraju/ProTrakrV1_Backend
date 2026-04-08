package com.klup.protrackr.dto.milestone;

public record MilestoneUpdateRequest(
        String title,
        Boolean completed,
        Integer sortOrder
) {}

