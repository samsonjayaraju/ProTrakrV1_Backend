package com.klup.protrackr.dto.feedback;

import jakarta.validation.constraints.NotBlank;

public record FeedbackUpdateRequest(
        @NotBlank String text
) {}

