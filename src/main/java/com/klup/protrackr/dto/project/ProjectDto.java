package com.klup.protrackr.dto.project;

import com.klup.protrackr.dto.common.SimpleUserDto;

import java.time.Instant;
import java.time.LocalDate;

public record ProjectDto(
        Long id,
        Long userId,
        SimpleUserDto owner,
        String title,
        String description,
        String category,
        String status,
        Integer progress,
        LocalDate dueDate,
        String techStack,
        String sourceUrl,
        String demoUrl,
        Instant createdAt,
        Instant updatedAt
) {}

