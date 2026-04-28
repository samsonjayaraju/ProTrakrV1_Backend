package com.klup.protrackr.repo;

import com.klup.protrackr.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProjectIdOrderByCreatedAtDesc(Long projectId);
    long countByStatusIgnoreCase(String status);

    @Query("select r from Review r join fetch r.project p join fetch p.owner o join fetch r.reviewer rv")
    List<Review> findAllWithJoins();
}
