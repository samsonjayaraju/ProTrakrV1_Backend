package com.klup.protrackr.dto.user;

public record UpdatePreferencesRequest(
        Boolean projectUpdatesEmail,
        Boolean milestoneRemindersEmail,
        Boolean platformAnnouncementsEmail,
        Boolean publicPortfolioEnabled
) {}

