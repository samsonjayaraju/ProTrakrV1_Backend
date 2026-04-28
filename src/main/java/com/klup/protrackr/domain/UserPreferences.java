package com.klup.protrackr.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_preferences")
public class UserPreferences {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "project_updates_email", nullable = false)
    private Boolean projectUpdatesEmail = Boolean.TRUE;

    @Column(name = "milestone_reminders_email", nullable = false)
    private Boolean milestoneRemindersEmail = Boolean.TRUE;

    @Column(name = "platform_announcements_email", nullable = false)
    private Boolean platformAnnouncementsEmail = Boolean.TRUE;

    @PrePersist
    public void prePersist() {
        if (projectUpdatesEmail == null) projectUpdatesEmail = Boolean.TRUE;
        if (milestoneRemindersEmail == null) milestoneRemindersEmail = Boolean.TRUE;
        if (platformAnnouncementsEmail == null) platformAnnouncementsEmail = Boolean.TRUE;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Boolean getProjectUpdatesEmail() {
        return projectUpdatesEmail;
    }

    public void setProjectUpdatesEmail(Boolean projectUpdatesEmail) {
        this.projectUpdatesEmail = projectUpdatesEmail;
    }

    public Boolean getMilestoneRemindersEmail() {
        return milestoneRemindersEmail;
    }

    public void setMilestoneRemindersEmail(Boolean milestoneRemindersEmail) {
        this.milestoneRemindersEmail = milestoneRemindersEmail;
    }

    public Boolean getPlatformAnnouncementsEmail() {
        return platformAnnouncementsEmail;
    }

    public void setPlatformAnnouncementsEmail(Boolean platformAnnouncementsEmail) {
        this.platformAnnouncementsEmail = platformAnnouncementsEmail;
    }
}

