package com.miniquests.quest.application.exception;

public class UserQuestNotFoundException extends RuntimeException {
    public UserQuestNotFoundException(Long userId, Long questId) {
        super("퀘스트를 찾을 수 없습니다. userId=" + userId + ", questId=" + questId);
    }
}
