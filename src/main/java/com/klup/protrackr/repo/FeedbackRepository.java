package com.klup.protrackr.repo;

import com.klup.protrackr.domain.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByProjectIdOrderByCreatedAtDesc(Long projectId);
    long countByProjectOwnerId(Long ownerId);
}

