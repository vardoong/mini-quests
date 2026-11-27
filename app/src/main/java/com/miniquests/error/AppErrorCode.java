package com.miniquests.error;

import com.miniquests.common.error.ErrorCode;
import com.miniquests.common.error.ErrorType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AppErrorCode implements ErrorCode {

    MISSING_REQUIRED_CLAIMS(ErrorType.UNAUTHORIZED, "APP-001", "인증 토큰에 필수 사용자 정보가 없습니다."),
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
