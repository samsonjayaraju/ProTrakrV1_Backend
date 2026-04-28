package com.klup.protrackr.dto.admin;

public record StudentDirectoryItemDto(
        Long id,
        String fullName,
        String email,
        String rollNumber,
        String department,
        Integer year,
        String avatarUrl,
        long projectCount,
        long completedProjectCount,
        String topSkill,
        String publicPortfolioUrl
) {}

