package com.klup.protrackr.dto.review;

import com.fasterxml.jackson.annotation.JsonAlias;

public record ReviewUpdateRequest(
        Integer technicalScore,
        Integer documentationScore,
        Integer innovationScore,
        Integer uiUxScore,
        @JsonAlias({"feedback"})
        String comments,
        String status
) {}

