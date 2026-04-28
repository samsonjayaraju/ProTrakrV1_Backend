package com.klup.protrackr.dto.review;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;

public record ReviewCreateRequest(
        @NotNull Long projectId,
        Integer technicalScore,
        Integer documentationScore,
        Integer innovationScore,
        Integer uiUxScore,
        @JsonAlias({"feedback"})
        String comments,
        String status
) {}
