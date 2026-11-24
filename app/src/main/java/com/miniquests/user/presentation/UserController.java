package com.miniquests.user.presentation;

import com.miniquests.user.application.UserService;
import com.miniquests.user.domain.User;
import com.miniquests.user.presentation.dto.MeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URL;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public MeResponse getMe(@AuthenticationPrincipal Jwt jwt) {
        String displayName = jwt.getClaimAsString("name");

        if (displayName == null || displayName.isBlank()) {
            displayName = jwt.getClaimAsString("preferred_username");
        }

        URL issuer = jwt.getIssuer();
        String subject = jwt.getSubject();
        if (issuer == null || subject == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token must contain iss and sub claims.");
        }
        User user = userService.getOrCreate(
                displayName,
                issuer.toString(),
                subject,
                jwt.getClaimAsString("email")
        );

        return MeResponse.from(user);
    }
}
