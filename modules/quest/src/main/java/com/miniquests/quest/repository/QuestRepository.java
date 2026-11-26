package com.miniquests.quest.repository;

import com.miniquests.quest.domain.Quest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestRepository extends JpaRepository<Quest, Long> {

    List<Quest> findByIsActiveTrueOrderByIdAsc();

    Optional<Quest> findByIdAndIsActiveTrue(Long id);
}
