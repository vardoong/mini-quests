package com.miniquests.error;

import com.miniquests.common.error.ErrorCode;
import com.miniquests.common.error.ErrorType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AppErrorCode implements ErrorCode {

    MISSING_REQUIRED_CLAIMS(ErrorType.UNAUTHORIZED, "APP-001", "인증 토큰에 필수 사용자 정보가 없습니다."),
    AUTHENTICATION_REQUIRED(ErrorType.UNAUTHORIZED, "APP-002", "인증이 필요합니다."),
    INVALID_REQUEST(ErrorType.INVALID, "APP-003", "요청 값이 유효하지 않습니다."),
    INTERNAL_SERVER_ERROR(ErrorType.INTERNAL_SERVER_ERROR, "APP-004", "서버 내부 오류가 발생했습니다."),
    ENDPOINT_NOT_FOUND(ErrorType.NOT_FOUND, "APP-005", "요청한 경로를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(ErrorType.METHOD_NOT_ALLOWED, "APP-006", "지원하지 않는 HTTP 메서드입니다."),
    UNCLASSIFIED_CLIENT_ERROR(ErrorType.INVALID, "APP-007", "요청을 처리할 수 없습니다."),
    SUSPENDED_USER(ErrorType.FORBIDDEN, "APP-008", "정지된 회원입니다."),
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
