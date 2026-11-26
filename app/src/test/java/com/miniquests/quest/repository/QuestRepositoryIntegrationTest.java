package com.miniquests.quest.repository;

import com.miniquests.quest.domain.Quest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class QuestRepositoryIntegrationTest {

    @Autowired
    private QuestRepository questRepository;

    @Test
    @DisplayName("활성 퀘스트 목록에는 비활성 퀘스트가 포함되지 않는다")
    void findsOnlyActiveQuests() {
        Quest activeQuest = Quest.builder()
                .title("활성 퀘스트")
                .description("조회할 수 있는 퀘스트")
                .build();
        activeQuest.active();

        Quest inactiveQuest = Quest.builder()
                .title("비활성 퀘스트")
                .description("조회할 수 없는 퀘스트")
                .build();

        Quest savedActiveQuest = questRepository.save(activeQuest);
        Quest savedInactiveQuest = questRepository.save(inactiveQuest);
        questRepository.flush();

        var foundQuests = questRepository.findByIsActiveTrueOrderByIdAsc();

        assertTrue(foundQuests.stream().anyMatch(quest ->
                quest.getId().equals(savedActiveQuest.getId())
        ));
        assertFalse(foundQuests.stream().anyMatch(quest ->
                quest.getId().equals(savedInactiveQuest.getId())
        ));
    }

    @Test
    @DisplayName("비활성 퀘스트는 ID로 조회되지 않는다")
    void doesNotFindInactiveQuestById() {
        Quest inactiveQuest = questRepository.save(
                Quest.builder()
                        .title("비활성 퀘스트")
                        .description("조회할 수 없는 퀘스트")
                        .build()
        );
        questRepository.flush();

        assertTrue(questRepository.findByIdAndIsActiveTrue(inactiveQuest.getId()).isEmpty());
    }
}
