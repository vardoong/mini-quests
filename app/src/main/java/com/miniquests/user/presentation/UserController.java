package com.miniquests.user.presentation;

import com.miniquests.security.JwtUserResolver;
import com.miniquests.user.domain.User;
import com.miniquests.user.presentation.dto.MeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final JwtUserResolver jwtUserResolver;

    @GetMapping("/me")
    public MeResponse getMe(@AuthenticationPrincipal Jwt jwt) {
        User user = jwtUserResolver.resolve(jwt);
        return MeResponse.from(user);
    }
}
