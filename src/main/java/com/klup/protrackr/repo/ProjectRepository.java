package com.klup.protrackr.repo;

import com.klup.protrackr.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByOwnerIdOrderByUpdatedAtDesc(Long ownerId);

    @Query("select p from Project p order by p.updatedAt desc")
    List<Project> findAllOrderByUpdatedAtDesc();

    long countByOwnerId(Long ownerId);
}

