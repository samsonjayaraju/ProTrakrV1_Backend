package com.klup.protrackr.dto.project;

import jakarta.validation.constraints.NotBlank;

public record UpdateReviewStatusRequest(
        @NotBlank String status
) {}

