package com.miniquests.quest.application;

import com.miniquests.quest.domain.Quest;
import com.miniquests.quest.domain.UserQuest;
import com.miniquests.quest.repository.QuestRepository;
import com.miniquests.quest.repository.UserQuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestQueryService {

    private final QuestRepository questRepository;
    private final UserQuestRepository userQuestRepository;

    public List<Quest> findActiveQuests() {
        return questRepository.findByIsActiveTrueOrderByIdAsc();
    }

    public Optional<Quest> findActiveQuest(Long questId) {
        return questRepository.findByIdAndIsActiveTrue(questId);
    }

    public List<UserQuest> findMyQuests(Long userId) {
        return userQuestRepository.findAllByUserIdOrderByStartedAtDesc(userId);
    }

}
