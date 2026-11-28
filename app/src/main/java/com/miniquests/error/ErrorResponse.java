package com.miniquests.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

public record ErrorResponse(
        String code,
        String message,
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        Map<String, Object> details
) {
    public ErrorResponse(String code, String message) {
        this(code, message, null);
    }
}