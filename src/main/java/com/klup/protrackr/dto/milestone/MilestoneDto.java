package com.klup.protrackr.dto.milestone;

public record MilestoneDto(
        Long id,
        Long projectId,
        String title,
        Boolean completed,
        Integer sortOrder
) {}

