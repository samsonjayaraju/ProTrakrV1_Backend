package com.klup.protrackr.service;

import com.klup.protrackr.domain.PortfolioProfile;
import com.klup.protrackr.domain.Project;
import com.klup.protrackr.domain.Review;
import com.klup.protrackr.domain.User;
import com.klup.protrackr.domain.UserRole;
import com.klup.protrackr.exception.ForbiddenException;
import com.klup.protrackr.repo.PortfolioProfileRepository;
import com.klup.protrackr.repo.ProjectRepository;
import com.klup.protrackr.repo.ReviewRepository;
import com.klup.protrackr.repo.SkillRepository;
import com.klup.protrackr.repo.UserRepository;
import com.klup.protrackr.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ExportService {
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ReviewRepository reviewRepository;
    private final SkillRepository skillRepository;
    private final PortfolioProfileRepository portfolioProfileRepository;

    public ExportService(UserRepository userRepository,
                         ProjectRepository projectRepository,
                         ReviewRepository reviewRepository,
                         SkillRepository skillRepository,
                         PortfolioProfileRepository portfolioProfileRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.reviewRepository = reviewRepository;
        this.skillRepository = skillRepository;
        this.portfolioProfileRepository = portfolioProfileRepository;
    }

    private void assertPrivileged(UserPrincipal principal) {
        if (principal.getRole() != UserRole.ADMIN && principal.getRole() != UserRole.FACULTY) {
            throw new ForbiddenException("Forbidden");
        }
    }

    @Transactional(readOnly = true)
    public String exportStudentsCsv(UserPrincipal principal, String search, String department, Integer year) {
        assertPrivileged(principal);
        List<User> students = userRepository.searchByRole(UserRole.STUDENT, emptyToNull(search), emptyToNull(department), year,
                org.springframework.data.domain.Pageable.unpaged()).getContent();

        Map<Long, Long> projectCount = toCountMap(projectRepository.countProjectsByOwner());
        Map<Long, Long> completedCount = toCountMap(projectRepository.countProjectsByOwnerAndStatus(AdminService.STATUS_COMPLETED));
        Map<Long, Long> inProgressCount = toCountMap(projectRepository.countProjectsByOwnerAndStatus(AdminService.STATUS_IN_PROGRESS));

        Map<Long, String> topSkill = new HashMap<>();
        for (Object[] row : skillRepository.topSkillsByUser()) {
            Long uid = ((Number) row[0]).longValue();
            if (!topSkill.containsKey(uid)) topSkill.put(uid, (String) row[1]);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("id,fullName,email,rollNumber,department,year,projectCount,completedProjects,inProgressProjects,topSkill,createdAt\n");
        for (User u : students) {
            sb.append(csv(u.getId())).append(',')
                    .append(csv(u.getFullName())).append(',')
                    .append(csv(u.getEmail())).append(',')
                    .append(csv(u.getRollNumber())).append(',')
                    .append(csv(u.getDepartment())).append(',')
                    .append(csv(u.getYear())).append(',')
                    .append(csv(projectCount.getOrDefault(u.getId(), 0L))).append(',')
                    .append(csv(completedCount.getOrDefault(u.getId(), 0L))).append(',')
                    .append(csv(inProgressCount.getOrDefault(u.getId(), 0L))).append(',')
                    .append(csv(topSkill.get(u.getId()))).append(',')
                    .append(csv(u.getCreatedAt()))
                    .append('\n');
        }
        return sb.toString();
    }

    @Transactional(readOnly = true)
    public String exportProjectsCsv(UserPrincipal principal,
                                   String search,
                                   String department,
                                   Integer year,
                                   String status,
                                   LocalDate fromDate,
                                   LocalDate toDate) {
        assertPrivileged(principal);
        List<Project> projects = projectRepository.findAllOrderByUpdatedAtDesc();
        Instant from = fromDate == null ? null : fromDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant to = toDate == null ? null : toDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        String s = emptyToNull(search);
        String dept = emptyToNull(department);
        String st = emptyToNull(status);

        StringBuilder sb = new StringBuilder();
        sb.append("id,title,studentName,studentEmail,category,status,progress,department,year,dueDate,createdAt,updatedAt\n");
        for (Project p : projects) {
            User o = p.getOwner();
            if (dept != null && !Objects.equals(o.getDepartment(), dept)) continue;
            if (year != null && !Objects.equals(o.getYear(), year)) continue;
            if (st != null && (p.getStatus() == null || !p.getStatus().equalsIgnoreCase(st))) continue;
            if (from != null && p.getCreatedAt().isBefore(from)) continue;
            if (to != null && !p.getCreatedAt().isBefore(to)) continue;
            if (s != null) {
                String hay = ((p.getTitle() == null ? "" : p.getTitle()) + " " + (o.getFullName() == null ? "" : o.getFullName()) + " " + (o.getEmail() == null ? "" : o.getEmail()))
                        .toLowerCase();
                if (!hay.contains(s.toLowerCase())) continue;
            }

            sb.append(csv(p.getId())).append(',')
                    .append(csv(p.getTitle())).append(',')
                    .append(csv(o.getFullName())).append(',')
                    .append(csv(o.getEmail())).append(',')
                    .append(csv(p.getCategory())).append(',')
                    .append(csv(p.getStatus())).append(',')
                    .append(csv(p.getProgress())).append(',')
                    .append(csv(o.getDepartment())).append(',')
                    .append(csv(o.getYear())).append(',')
                    .append(csv(p.getDueDate())).append(',')
                    .append(csv(p.getCreatedAt())).append(',')
                    .append(csv(p.getUpdatedAt()))
                    .append('\n');
        }
        return sb.toString();
    }

    @Transactional(readOnly = true)
    public String exportReviewsCsv(UserPrincipal principal, String status, LocalDate fromDate, LocalDate toDate) {
        assertPrivileged(principal);
        List<Review> reviews = reviewRepository.findAllWithJoins();
        Instant from = fromDate == null ? null : fromDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant to = toDate == null ? null : toDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        String st = emptyToNull(status);

        StringBuilder sb = new StringBuilder();
        sb.append("id,projectId,projectTitle,studentName,reviewerName,score,status,feedback,createdAt\n");
        for (Review r : reviews) {
            if (st != null && (r.getStatus() == null || !r.getStatus().equalsIgnoreCase(st))) continue;
            if (from != null && r.getCreatedAt().isBefore(from)) continue;
            if (to != null && !r.getCreatedAt().isBefore(to)) continue;

            sb.append(csv(r.getId())).append(',')
                    .append(csv(r.getProject().getId())).append(',')
                    .append(csv(r.getProject().getTitle())).append(',')
                    .append(csv(r.getProject().getOwner().getFullName())).append(',')
                    .append(csv(r.getReviewer().getFullName())).append(',')
                    .append(csv(r.getTotalScore())).append(',')
                    .append(csv(r.getStatus())).append(',')
                    .append(csv(r.getComments())).append(',')
                    .append(csv(r.getCreatedAt()))
                    .append('\n');
        }
        return sb.toString();
    }

    @Transactional(readOnly = true)
    public String exportReportsCsv(UserPrincipal principal) {
        assertPrivileged(principal);

        long totalStudents = userRepository.countByRole(UserRole.STUDENT);
        long totalProjects = projectRepository.count();
        long completedProjects = projectRepository.countByStatusIgnoreCase(AdminService.STATUS_COMPLETED);
        long pendingReviews = reviewRepository.countByStatusIgnoreCase(AdminService.STATUS_PENDING_REVIEW);

        Map<String, Long> studentsByDept = toStringCountMap(userRepository.countStudentsByDepartment());
        Map<String, Long> projectsByDept = toStringCountMap(projectRepository.countProjectsByDepartment());
        Map<String, Long> completedByDept = toStringCountMap(projectRepository.countProjectsByDepartmentAndStatus(AdminService.STATUS_COMPLETED));

        StringBuilder sb = new StringBuilder();
        sb.append("metric,value\n");
        sb.append("totalStudents,").append(totalStudents).append('\n');
        sb.append("totalProjects,").append(totalProjects).append('\n');
        sb.append("completedProjects,").append(completedProjects).append('\n');
        sb.append("pendingReviews,").append(pendingReviews).append('\n');
        sb.append('\n');
        sb.append("department,students,projects,completedProjects\n");

        for (String dept : studentsByDept.keySet()) {
            sb.append(csv(dept)).append(',')
                    .append(csv(studentsByDept.getOrDefault(dept, 0L))).append(',')
                    .append(csv(projectsByDept.getOrDefault(dept, 0L))).append(',')
                    .append(csv(completedByDept.getOrDefault(dept, 0L)))
                    .append('\n');
        }
        return sb.toString();
    }

    private static String csv(Object v) {
        if (v == null) return "";
        String s = String.valueOf(v);
        boolean needsQuote = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        if (!needsQuote) return s;
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    private static Map<Long, Long> toCountMap(List<Object[]> rows) {
        Map<Long, Long> map = new HashMap<>();
        for (Object[] r : rows) {
            map.put(((Number) r[0]).longValue(), ((Number) r[1]).longValue());
        }
        return map;
    }

    private static Map<String, Long> toStringCountMap(List<Object[]> rows) {
        Map<String, Long> map = new HashMap<>();
        for (Object[] r : rows) {
            String key = (String) r[0];
            if (key == null) key = "Unknown";
            map.put(key, ((Number) r[1]).longValue());
        }
        return map;
    }

    private static String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isBlank() ? null : t;
    }
}

