package com.miniquests.quest.application;

import com.miniquests.common.error.BusinessException;
import com.miniquests.quest.domain.Quest;
import com.miniquests.quest.domain.UserQuest;
import com.miniquests.quest.domain.UserQuestStatus;
import com.miniquests.quest.error.QuestErrorCode;
import com.miniquests.quest.repository.QuestRepository;
import com.miniquests.quest.repository.UserQuestRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestCommandServiceTest {

    @Mock
    private QuestRepository questRepository;

    @Mock
    private UserQuestRepository userQuestRepository;

    @InjectMocks
    private QuestCommandService questCommandService;

    @Test
    @DisplayName("활성 퀘스트를 처음 시작하면 진행 정보를 저장한다")
    void startsActiveQuest() {
        Long userId = 10L;
        Long questId = 20L;
        Quest quest = activeQuest();

        when(questRepository.findByIdAndIsActiveTrue(questId))
                .thenReturn(Optional.of(quest));
        when(userQuestRepository.existsByUserIdAndQuest_Id(userId, questId))
                .thenReturn(false);

        questCommandService.start(userId, questId);

        ArgumentCaptor<UserQuest> userQuestCaptor = ArgumentCaptor.forClass(UserQuest.class);
        verify(userQuestRepository).save(userQuestCaptor.capture());

        UserQuest savedUserQuest = userQuestCaptor.getValue();
        assertEquals(userId, savedUserQuest.getUserId());
        assertSame(quest, savedUserQuest.getQuest());
        assertEquals(UserQuestStatus.IN_PROGRESS, savedUserQuest.getStatus());
    }

    @Test
    @DisplayName("활성 퀘스트가 없으면 시작할 수 없다")
    void doesNotStartWhenActiveQuestDoesNotExist() {
        Long userId = 10L;
        Long questId = 20L;
        when(questRepository.findByIdAndIsActiveTrue(questId))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> questCommandService.start(userId, questId)
        );
        assertEquals(QuestErrorCode.ACTIVE_QUEST_NOT_FOUND, exception.getErrorCode());
        assertEquals(Map.of(), exception.getDetails());

        verify(userQuestRepository, never()).existsByUserIdAndQuest_Id(userId, questId);
        verify(userQuestRepository, never()).save(any(UserQuest.class));
    }

    @Test
    @DisplayName("이미 시작한 퀘스트는 다시 시작할 수 없다")
    void doesNotStartAlreadyStartedQuest() {
        Long userId = 10L;
        Long questId = 20L;
        when(questRepository.findByIdAndIsActiveTrue(questId))
                .thenReturn(Optional.of(activeQuest()));
        when(userQuestRepository.existsByUserIdAndQuest_Id(userId, questId))
                .thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> questCommandService.start(userId, questId)
        );
        assertEquals(QuestErrorCode.QUEST_ALREADY_STARTED, exception.getErrorCode());
        assertEquals(Map.of(), exception.getDetails());

        verify(userQuestRepository, never()).save(any(UserQuest.class));
    }

    @Test
    @DisplayName("진행 중인 퀘스트를 완료하고 완료 시각을 기록한다")
    void completesUserQuest() {
        Long userId = 10L;
        Long questId = 20L;
        Instant completedAt = Instant.parse("2026-11-26T10:00:00Z");
        UserQuest userQuest = userQuest(userId, activeQuest());

        when(userQuestRepository.findByUserIdAndQuest_Id(userId, questId))
                .thenReturn(Optional.of(userQuest));

        questCommandService.complete(userId, questId, completedAt);

        assertEquals(UserQuestStatus.COMPLETED, userQuest.getStatus());
        assertEquals(completedAt, userQuest.getCompletedAt());
        verify(userQuestRepository).findByUserIdAndQuest_Id(userId, questId);
    }

    @Test
    @DisplayName("사용자의 퀘스트 진행 정보가 없으면 완료할 수 없다")
    void doesNotCompleteWhenUserQuestDoesNotExist() {
        Long userId = 10L;
        Long questId = 20L;
        Instant completedAt = Instant.parse("2026-11-26T10:00:00Z");
        when(userQuestRepository.findByUserIdAndQuest_Id(userId, questId))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> questCommandService.complete(userId, questId, completedAt)
        );
        assertEquals(QuestErrorCode.USER_QUEST_NOT_FOUND, exception.getErrorCode());
        assertEquals(Map.of(), exception.getDetails());
    }

    @Test
    @DisplayName("이미 완료된 퀘스트는 다시 완료할 수 없다")
    void doesNotCompleteAlreadyCompletedQuest() {
        Long userId = 10L;
        Long questId = 20L;
        Instant firstCompletedAt = Instant.parse("2026-11-25T10:00:00Z");
        Instant secondCompletedAt = Instant.parse("2026-11-26T10:00:00Z");
        UserQuest userQuest = userQuest(userId, activeQuest());
        userQuest.complete(firstCompletedAt);

        when(userQuestRepository.findByUserIdAndQuest_Id(userId, questId))
                .thenReturn(Optional.of(userQuest));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> questCommandService.complete(userId, questId, secondCompletedAt)
        );
        assertEquals(QuestErrorCode.QUEST_ALREADY_COMPLETED, exception.getErrorCode());
        assertEquals(Map.of(), exception.getDetails());

        assertEquals(UserQuestStatus.COMPLETED, userQuest.getStatus());
        assertEquals(firstCompletedAt, userQuest.getCompletedAt());
    }

    private UserQuest userQuest(Long userId, Quest quest) {
        return UserQuest.builder()
                .userId(userId)
                .quest(quest)
                .build();
    }

    private Quest activeQuest() {
        Quest quest = Quest.builder()
                .title("테스트 퀘스트")
                .description("시작 서비스 테스트")
                .build();
        quest.active();
        return quest;
    }
}
