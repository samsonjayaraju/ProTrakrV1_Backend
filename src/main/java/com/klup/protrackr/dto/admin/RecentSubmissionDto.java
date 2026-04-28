package com.klup.protrackr.dto.admin;

import java.time.Instant;

public record RecentSubmissionDto(
        Long id,
        String title,
        String studentName,
        String studentAvatarUrl,
        Instant submittedAt,
        String status
) {}

