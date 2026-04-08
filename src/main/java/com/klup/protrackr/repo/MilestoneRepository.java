package com.klup.protrackr.repo;

import com.klup.protrackr.domain.Milestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MilestoneRepository extends JpaRepository<Milestone, Long> {
    @Query("select m from Milestone m where m.project.id = :projectId order by coalesce(m.sortOrder, 2147483647) asc, m.id asc")
    List<Milestone> findByProjectIdOrdered(Long projectId);

    long countByProjectOwnerId(Long ownerId);

    long countByProjectOwnerIdAndCompletedTrue(Long ownerId);
}

