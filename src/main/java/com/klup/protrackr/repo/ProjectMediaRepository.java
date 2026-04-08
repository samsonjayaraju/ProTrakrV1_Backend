package com.klup.protrackr.repo;

import com.klup.protrackr.domain.ProjectMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectMediaRepository extends JpaRepository<ProjectMedia, Long> {
    List<ProjectMedia> findByProjectIdOrderByCreatedAtDesc(Long projectId);
}

