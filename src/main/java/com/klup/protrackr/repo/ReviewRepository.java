package com.klup.protrackr.repo;

import com.klup.protrackr.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProjectIdOrderByCreatedAtDesc(Long projectId);
    long countByStatusIgnoreCase(String status);
}

