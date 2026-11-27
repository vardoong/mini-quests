package com.miniquests.common.error;

import lombok.Getter;

import java.util.Map;

@Getter
public class BusinessException extends RuntimeException {
    @SuppressWarnings("java:S1948")
    private final ErrorCode errorCode;

    @SuppressWarnings("java:S1948")
    private final Map<String, Object> details;

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, Map.of());
    }

    public BusinessException(
            ErrorCode errorCode,
            Map<String, Object> details
    ) {
        super(errorCode.message());
        this.errorCode = errorCode;
        this.details = Map.copyOf(details);
    }
}
