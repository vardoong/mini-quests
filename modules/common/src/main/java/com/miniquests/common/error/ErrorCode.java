package com.miniquests.common.error;

public interface ErrorCode {
    ErrorType type();

    String code();

    String message();
}
