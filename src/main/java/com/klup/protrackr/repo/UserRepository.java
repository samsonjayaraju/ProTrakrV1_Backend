package com.klup.protrackr.repo;

import com.klup.protrackr.domain.User;
import com.klup.protrackr.domain.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);

    long countByRole(UserRole role);

    @Query("""
            select u from User u
            where u.role = :role
              and (:department is null or u.department = :department)
              and (:year is null or u.year = :year)
              and (
                    :search is null
                 or lower(u.fullName) like lower(concat('%', :search, '%'))
                 or lower(u.email) like lower(concat('%', :search, '%'))
                 or lower(u.rollNumber) like lower(concat('%', :search, '%'))
              )
            """)
    Page<User> searchByRole(@Param("role") UserRole role,
                            @Param("search") String search,
                            @Param("department") String department,
                            @Param("year") Integer year,
                            Pageable pageable);

    @Query(value = "select department, count(*) from users where role = 'student' group by department", nativeQuery = true)
    List<Object[]> countStudentsByDepartment();
}
