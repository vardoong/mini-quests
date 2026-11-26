package com.miniquests.quest.application.exception;

public class ActiveQuestNotFoundException extends RuntimeException {
    public ActiveQuestNotFoundException(Long questId) {
        super("활성 퀘스트를 찾을 수 없습니다. questId=" + questId);
    }
}
