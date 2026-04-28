package com.klup.protrackr.dto.project;

import com.klup.protrackr.dto.common.SimpleUserDto;
import com.klup.protrackr.dto.feedback.FeedbackDto;
import com.klup.protrackr.dto.milestone.MilestoneDto;
import com.klup.protrackr.dto.review.ReviewDto;
import com.klup.protrackr.dto.upload.ProjectMediaDto;

import java.util.List;

public record ProjectDetailsDto(
        ProjectDto project,
        List<MilestoneDto> milestones,
        List<FeedbackDto> feedback,
        List<ReviewDto> reviews,
        List<SimpleUserDto> team,
        List<ProjectMediaDto> media
) {}

