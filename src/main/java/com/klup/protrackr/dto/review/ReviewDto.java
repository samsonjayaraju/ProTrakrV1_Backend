package com.klup.protrackr.dto.review;

import com.klup.protrackr.dto.common.SimpleUserDto;

import java.time.Instant;

public record ReviewDto(
        Long id,
        Long projectId,
        SimpleUserDto reviewer,
        Integer technicalScore,
        Integer documentationScore,
        Integer innovationScore,
        Integer uiUxScore,
        Integer totalScore,
        String comments,
        String status,
        Instant createdAt,
        Instant updatedAt
) {}

