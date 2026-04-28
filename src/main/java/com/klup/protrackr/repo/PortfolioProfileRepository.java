package com.klup.protrackr.repo;

import com.klup.protrackr.domain.PortfolioProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortfolioProfileRepository extends JpaRepository<PortfolioProfile, Long> {
    Optional<PortfolioProfile> findByUserId(Long userId);
    Optional<PortfolioProfile> findBySlugIgnoreCase(String slug);
    boolean existsBySlugIgnoreCaseAndUserIdNot(String slug, Long userId);

    List<PortfolioProfile> findByUserIdIn(List<Long> userIds);
}
