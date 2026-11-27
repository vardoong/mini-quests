package com.miniquests.security;

import com.miniquests.common.error.BusinessException;
import com.miniquests.error.AppErrorCode;
import com.miniquests.user.application.UserService;
import com.miniquests.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.net.URL;

@Component
@RequiredArgsConstructor
public class JwtUserResolver {

    private final UserService userService;

    public User resolve(Jwt jwt) {
        String displayName = jwt.getClaimAsString("name");
        if (displayName == null || displayName.isBlank()) {
            displayName = jwt.getClaimAsString("preferred_username");
        }

        URL issuer = jwt.getIssuer();
        String subject = jwt.getSubject();

        if (issuer == null || subject == null) {
            throw new BusinessException(AppErrorCode.MISSING_REQUIRED_CLAIMS);
        }

        return userService.getOrCreate(
                displayName,
                issuer.toString(),
                subject,
                jwt.getClaimAsString("email")
        );
    }
}