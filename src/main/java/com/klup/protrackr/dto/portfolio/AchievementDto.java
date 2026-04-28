package com.klup.protrackr.dto.portfolio;

import java.time.LocalDate;

public record AchievementDto(
        String title,
        String issuer,
        LocalDate date,
        String description
) {}

