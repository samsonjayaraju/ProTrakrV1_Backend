package com.klup.protrackr.mapper;

import com.klup.protrackr.domain.Feedback;
import com.klup.protrackr.domain.Milestone;
import com.klup.protrackr.domain.Project;
import com.klup.protrackr.domain.ProjectMedia;
import com.klup.protrackr.domain.Review;
import com.klup.protrackr.domain.User;
import com.klup.protrackr.dto.common.SimpleUserDto;
import com.klup.protrackr.dto.feedback.FeedbackDto;
import com.klup.protrackr.dto.milestone.MilestoneDto;
import com.klup.protrackr.dto.project.ProjectDto;
import com.klup.protrackr.dto.review.ReviewDto;
import com.klup.protrackr.dto.upload.ProjectMediaDto;
import com.klup.protrackr.dto.user.UserDto;

public final class DtoMapper {
    private DtoMapper() {}

    public static SimpleUserDto simpleUser(User u) {
        return new SimpleUserDto(
                u.getId(),
                u.getFullName(),
                u.getEmail(),
                u.getRole().dbValue(),
                u.getAvatarUrl()
        );
    }

    public static UserDto userDto(User u) {
        String fullName = u.getFullName() == null ? "" : u.getFullName().trim();
        String firstName = null;
        String lastName = null;
        if (!fullName.isBlank()) {
            int idx = fullName.indexOf(' ');
            if (idx < 0) {
                firstName = fullName;
                lastName = "";
            } else {
                firstName = fullName.substring(0, idx).trim();
                lastName = fullName.substring(idx + 1).trim();
            }
        }
        return new UserDto(
                u.getId(),
                firstName,
                lastName,
                u.getFullName(),
                u.getEmail(),
                u.getRole().dbValue(),
                u.getDepartment(),
                u.getYear(),
                u.getRollNumber(),
                u.getBio(),
                u.getLocation(),
                u.getAvatarUrl(),
                u.getGithubUrl(),
                u.getPortfolioPublic(),
                u.getCreatedAt(),
                u.getUpdatedAt()
        );
    }

    public static ProjectDto projectDto(Project p) {
        User owner = p.getOwner();
        return new ProjectDto(
                p.getId(),
                owner.getId(),
                simpleUser(owner),
                p.getTitle(),
                p.getDescription(),
                p.getCategory(),
                p.getStatus(),
                p.getProgress(),
                p.getDueDate(),
                p.getTechStack(),
                p.getSourceUrl(),
                p.getDemoUrl(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }

    public static MilestoneDto milestoneDto(Milestone m) {
        return new MilestoneDto(
                m.getId(),
                m.getProject().getId(),
                m.getTitle(),
                m.getCompleted(),
                m.getSortOrder()
        );
    }

    public static FeedbackDto feedbackDto(Feedback f) {
        return new FeedbackDto(
                f.getId(),
                f.getProject().getId(),
                simpleUser(f.getAuthor()),
                f.getText(),
                f.getCreatedAt()
        );
    }

    public static ReviewDto reviewDto(Review r) {
        return new ReviewDto(
                r.getId(),
                r.getProject().getId(),
                simpleUser(r.getReviewer()),
                r.getTechnicalScore(),
                r.getDocumentationScore(),
                r.getInnovationScore(),
                r.getUiUxScore(),
                r.getTotalScore(),
                r.getComments(),
                r.getStatus(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }

    public static ProjectMediaDto projectMediaDto(ProjectMedia m) {
        return new ProjectMediaDto(
                m.getId(),
                m.getProject().getId(),
                simpleUser(m.getUploader()),
                m.getFileUrl(),
                m.getFileName(),
                m.getFileType(),
                m.getCreatedAt()
        );
    }
}
