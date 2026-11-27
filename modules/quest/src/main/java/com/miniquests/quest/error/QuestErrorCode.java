package com.miniquests.quest.error;

import com.miniquests.common.error.ErrorCode;
import com.miniquests.common.error.ErrorType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum QuestErrorCode implements ErrorCode {

    ACTIVE_QUEST_NOT_FOUND(ErrorType.NOT_FOUND, "QUE-001", "활성 퀘스트를 찾을 수 없습니다."),
    QUEST_ALREADY_STARTED(ErrorType.CONFLICT, "QUE-002", "이미 시작한 퀘스트입니다."),
    USER_QUEST_NOT_FOUND(ErrorType.NOT_FOUND, "QUE-003", "퀘스트를 찾을 수 없습니다."),
    QUEST_ALREADY_COMPLETED(ErrorType.CONFLICT, "QUE-004", "이미 완료된 퀘스트입니다."),
    ;

    private final ErrorType type;
    private final String code;
    private final String message;

    @Override
    public ErrorType type() {
        return type;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
