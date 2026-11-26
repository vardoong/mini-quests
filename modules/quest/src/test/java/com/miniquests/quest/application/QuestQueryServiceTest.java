package com.miniquests.quest.application;

import com.miniquests.quest.domain.Quest;
import com.miniquests.quest.domain.UserQuest;
import com.miniquests.quest.repository.QuestRepository;
import com.miniquests.quest.repository.UserQuestRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestQueryServiceTest {

    @Mock
    private QuestRepository questRepository;

    @Mock
    private UserQuestRepository userQuestRepository;

    @InjectMocks
    private QuestQueryService questQueryService;

    @Test
    @DisplayName("활성 퀘스트 목록을 반환한다")
    void findsActiveQuests() {
        Quest quest = Quest.builder()
                .title("활성 퀘스트")
                .description("조회 가능한 퀘스트")
                .build();
        quest.active();

        List<Quest> expected = List.of(quest);
        when(questRepository.findByIsActiveTrueOrderByIdAsc()).thenReturn(expected);

        assertEquals(expected, questQueryService.findActiveQuests());
        verify(questRepository).findByIsActiveTrueOrderByIdAsc();
    }

    @Test
    @DisplayName("활성 퀘스트 상세 정보를 반환한다")
    void findsActiveQuest() {
        Quest quest = Quest.builder()
                .title("활성 퀘스트")
                .description("조회 가능한 퀘스트")
                .build();
        quest.active();

        when(questRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(Optional.of(quest));

        assertEquals(Optional.of(quest), questQueryService.findActiveQuest(1L));
        verify(questRepository).findByIdAndIsActiveTrue(1L);
    }

    @Test
    @DisplayName("활성 퀘스트가 없으면 빈 결과를 반환한다")
    void returnsEmptyWhenActiveQuestDoesNotExist() {
        when(questRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(Optional.empty());

        assertEquals(Optional.empty(), questQueryService.findActiveQuest(1L));
    }

    @Test
    @DisplayName("회원의 퀘스트 진행 목록을 반환한다")
    void findsMyQuests() {
        Long userId = 10L;
        Quest quest = Quest.builder()
                .title("내 퀘스트")
                .description("진행 중인 퀘스트")
                .build();
        UserQuest userQuest = UserQuest.builder()
                .userId(userId)
                .quest(quest)
                .build();
        List<UserQuest> expected = List.of(userQuest);
        when(userQuestRepository.findAllByUserIdOrderByStartedAtDesc(userId))
                .thenReturn(expected);

        assertEquals(expected, questQueryService.findMyQuests(userId));
        verify(userQuestRepository).findAllByUserIdOrderByStartedAtDesc(userId);
    }

    @Test
    @DisplayName("회원의 퀘스트 진행 정보가 없으면 빈 목록을 반환한다")
    void returnsEmptyWhenMemberHasNoQuests() {
        Long userId = 10L;
        when(userQuestRepository.findAllByUserIdOrderByStartedAtDesc(userId))
                .thenReturn(List.of());

        assertEquals(List.of(), questQueryService.findMyQuests(userId));
        verify(userQuestRepository).findAllByUserIdOrderByStartedAtDesc(userId);
    }
}
