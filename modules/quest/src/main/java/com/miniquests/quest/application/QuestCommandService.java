package com.miniquests.quest.application;

import com.miniquests.quest.application.exception.ActiveQuestNotFoundException;
import com.miniquests.quest.application.exception.QuestAlreadyStartedException;
import com.miniquests.quest.application.exception.UserQuestNotFoundException;
import com.miniquests.quest.domain.Quest;
import com.miniquests.quest.domain.UserQuest;
import com.miniquests.quest.repository.QuestRepository;
import com.miniquests.quest.repository.UserQuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuestCommandService {

    private final QuestRepository questRepository;
    private final UserQuestRepository userQuestRepository;

    @Transactional
    public void start(Long userId, Long questId) {
        Optional<Quest> optionalQuest = questRepository.findByIdAndIsActiveTrue(questId);
        if (optionalQuest.isEmpty()) {
            throw new ActiveQuestNotFoundException(questId);
        }

        if (userQuestRepository.existsByUserIdAndQuest_Id(userId, questId)) {
            throw new QuestAlreadyStartedException(userId, questId);
        }

        Quest quest = optionalQuest.get();
        UserQuest userQuest = UserQuest.builder()
                .userId(userId)
                .quest(quest)
                .build();

        userQuestRepository.save(userQuest);

    }

    @Transactional
    public void complete(Long userId, Long questId, Instant completedAt) {
        UserQuest userQuest = userQuestRepository.findByUserIdAndQuest_Id(userId, questId)
                .orElseThrow(() -> new UserQuestNotFoundException(userId, questId));

        userQuest.complete(completedAt);
    }
}
