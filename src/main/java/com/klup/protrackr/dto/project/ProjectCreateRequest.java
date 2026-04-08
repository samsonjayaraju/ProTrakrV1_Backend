package com.klup.protrackr.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ProjectCreateRequest(
        @NotBlank @Size(max = 255) String title,
        String description,
        @Size(max = 100) String category,
        @Size(max = 50) String status,
        Integer progress,
        LocalDate dueDate,
        String techStack,
        @Size(max = 500) String sourceUrl,
        @Size(max = 500) String demoUrl
) {}

