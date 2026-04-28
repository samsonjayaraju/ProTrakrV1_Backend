package com.klup.protrackr.repo;

import com.klup.protrackr.domain.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    List<Skill> findByUserIdOrderByIdAsc(Long userId);
    void deleteByUserId(Long userId);

    @Query(value = "select user_id, name, count(*) as c from skills group by user_id, name order by user_id asc, c desc", nativeQuery = true)
    List<Object[]> topSkillsByUser();
}

