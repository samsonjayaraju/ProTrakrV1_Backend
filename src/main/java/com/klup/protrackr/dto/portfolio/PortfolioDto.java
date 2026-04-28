package com.klup.protrackr.dto.portfolio;

import com.klup.protrackr.dto.project.ProjectDto;

import java.util.List;

public record PortfolioDto(
        String fullName,
        String headline,
        String bio,
        String location,
        String email,
        String githubUrl,
        String linkedinUrl,
        String websiteUrl,
        String avatarUrl,
        String resumeUrl,
        Boolean publicProfileEnabled,
        String slug,
        List<SkillDto> skills,
        List<AchievementDto> achievements,
        List<ProjectDto> highlightedProjects
) {}

