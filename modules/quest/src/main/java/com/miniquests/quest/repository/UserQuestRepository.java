package com.miniquests.quest.repository;

import com.miniquests.quest.domain.UserQuest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserQuestRepository extends JpaRepository<UserQuest, Long> {

    boolean existsByUserIdAndQuest_Id(Long userId, Long questId);

    Optional<UserQuest> findByUserIdAndQuest_Id(Long userId, Long questId);

    @EntityGraph(attributePaths = "quest")
    List<UserQuest> findAllByUserIdOrderByStartedAtDesc(Long userId);
}
