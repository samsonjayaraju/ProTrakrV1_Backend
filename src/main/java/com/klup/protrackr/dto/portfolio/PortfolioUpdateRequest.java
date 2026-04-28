package com.klup.protrackr.dto.portfolio;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.List;

public record PortfolioUpdateRequest(
        @JsonAlias({"full_name", "name", "fullName"})
        String fullName,
        String headline,
        String bio,
        String location,
        String email,
        String githubUrl,
        String linkedinUrl,
        String websiteUrl,
        Boolean publicProfileEnabled,
        String slug,
        List<SkillDto> skills,
        List<AchievementDto> achievements,
        List<Long> highlightedProjectIds
) {}

