package com.klup.protrackr.dto.milestone;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MilestoneCreateRequest(
        @NotNull Long projectId,
        @NotBlank String title,
        Boolean completed,
        Integer sortOrder
) {}

