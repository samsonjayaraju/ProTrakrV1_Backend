package com.klup.protrackr.dto.review;

import jakarta.validation.constraints.NotNull;

public record ReviewCreateRequest(
        @NotNull Long projectId,
        Integer technicalScore,
        Integer documentationScore,
        Integer innovationScore,
        Integer uiUxScore,
        String comments,
        String status
) {}

