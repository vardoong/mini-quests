package com.miniquests.quest.application.exception;

public class QuestAlreadyStartedException extends RuntimeException {
    public QuestAlreadyStartedException(Long userId, Long questId) {
        super("이미 시작한 퀘스트입니다. userId=" + userId + ", questId=" + questId);
    }
}
