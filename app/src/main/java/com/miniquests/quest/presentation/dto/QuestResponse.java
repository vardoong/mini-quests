package com.miniquests.quest.presentation.dto;

import com.miniquests.quest.domain.Quest;

public record QuestResponse(
        Long id,
        String title,
        String description
) {
    public static QuestResponse from(Quest quest) {
        return new QuestResponse(
                quest.getId(),
                quest.getTitle(),
                quest.getDescription()
        );
    }
}
