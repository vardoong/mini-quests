package com.miniquests.user.error;

import com.miniquests.common.error.ErrorCode;
import com.miniquests.common.error.ErrorType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    USER_QUEST_NOT_FOUND(ErrorType.NOT_FOUND, "USE-001", "사용자 정보가 없습니다."),
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
