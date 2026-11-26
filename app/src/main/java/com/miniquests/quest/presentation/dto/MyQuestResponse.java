package com.miniquests.quest.presentation.dto;

import com.miniquests.quest.domain.UserQuest;
import com.miniquests.quest.domain.UserQuestStatus;

import java.time.Instant;

public record MyQuestResponse(
        Long questId,
        String title,
        UserQuestStatus userQuestStatus,
        Instant startedAt,
        Instant completedAt
) {
    public static MyQuestResponse from(UserQuest userQuest) {
        return new MyQuestResponse(
                userQuest.getQuest().getId(),
                userQuest.getQuest().getTitle(),
                userQuest.getStatus(),
                userQuest.getStartedAt(),
                userQuest.getCompletedAt()
        );
    }
}
