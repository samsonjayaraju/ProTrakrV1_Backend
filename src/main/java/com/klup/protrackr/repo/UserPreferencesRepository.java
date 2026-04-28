package com.klup.protrackr.repo;

import com.klup.protrackr.domain.UserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Long> {
}

