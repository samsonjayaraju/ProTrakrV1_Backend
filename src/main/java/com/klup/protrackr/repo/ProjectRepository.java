package com.klup.protrackr.repo;

import com.klup.protrackr.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByOwnerIdOrderByUpdatedAtDesc(Long ownerId);

    @Query("select p from Project p order by p.updatedAt desc")
    List<Project> findAllOrderByUpdatedAtDesc();

    long countByOwnerId(Long ownerId);

    long countByStatusIgnoreCase(String status);

    @Query("select p from Project p order by p.updatedAt desc")
    List<Project> findRecent(org.springframework.data.domain.Pageable pageable);

    @Query(value = "select user_id, count(*) from projects group by user_id", nativeQuery = true)
    List<Object[]> countProjectsByOwner();

    @Query(value = "select user_id, count(*) from projects where upper(status) = upper(:status) group by user_id", nativeQuery = true)
    List<Object[]> countProjectsByOwnerAndStatus(@Param("status") String status);

    @Query(value = "select count(*) from project_team where project_id = :projectId and user_id = :userId", nativeQuery = true)
    long isUserInTeam(@Param("projectId") Long projectId, @Param("userId") Long userId);

    @Query(value = "select u.department, count(*) from projects p join users u on p.user_id = u.id group by u.department", nativeQuery = true)
    List<Object[]> countProjectsByDepartment();

    @Query(value = "select u.department, count(*) from projects p join users u on p.user_id = u.id where upper(p.status) = upper(:status) group by u.department", nativeQuery = true)
    List<Object[]> countProjectsByDepartmentAndStatus(@Param("status") String status);
}
