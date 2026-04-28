package com.klup.protrackr.repo;

import com.klup.protrackr.domain.HighlightedProject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HighlightedProjectRepository extends JpaRepository<HighlightedProject, Long> {
    List<HighlightedProject> findByUserIdOrderBySortOrderAscIdAsc(Long userId);
    void deleteByUserId(Long userId);
}

