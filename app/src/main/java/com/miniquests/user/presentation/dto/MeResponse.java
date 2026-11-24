package com.miniquests.user.presentation.dto;

import com.miniquests.user.domain.User;
import com.miniquests.user.domain.UserStatus;

public record MeResponse(
        Long id,
        String displayName,
        String email,
        UserStatus status
) {
    public static MeResponse from(User user) {
        return new MeResponse(
                user.getId(),
                user.getDisplayName(),
                user.getEmail(),
                user.getStatus()
        );
    }
}
