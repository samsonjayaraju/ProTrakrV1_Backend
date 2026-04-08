package com.klup.protrackr.dto.feedback;

import com.klup.protrackr.dto.common.SimpleUserDto;

import java.time.Instant;

public record FeedbackDto(
        Long id,
        Long projectId,
        SimpleUserDto author,
        String text,
        Instant createdAt
) {}

