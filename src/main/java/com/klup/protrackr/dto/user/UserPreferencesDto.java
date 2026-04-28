package com.klup.protrackr.dto.user;

public record UserPreferencesDto(
        Boolean projectUpdatesEmail,
        Boolean milestoneRemindersEmail,
        Boolean platformAnnouncementsEmail,
        Boolean publicPortfolioEnabled
) {}

