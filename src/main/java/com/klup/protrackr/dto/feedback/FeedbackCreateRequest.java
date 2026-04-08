package com.klup.protrackr.dto.feedback;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FeedbackCreateRequest(
        @NotNull Long projectId,
        @NotBlank String text
) {}

