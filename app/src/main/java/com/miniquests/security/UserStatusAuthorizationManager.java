package com.miniquests.security;

import com.miniquests.user.application.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class UserStatusAuthorizationManager
        implements AuthorizationManager<RequestAuthorizationContext> {

    private final UserService userService;

    @Override
    public AuthorizationResult authorize(
            Supplier<? extends Authentication> authentication,
            RequestAuthorizationContext context
    ) {
        Authentication currentAuthentication = authentication.get();

        if (currentAuthentication == null
                || !(currentAuthentication.getPrincipal() instanceof Jwt jwt)) {
            return new AuthorizationDecision(false);
        }

        URL issuer = jwt.getIssuer();
        String subject = jwt.getSubject();

        // 필수 claim 검증은 JWT를 사용하는 서비스 경계(JwtUserResolver)에서 처리
        // 회원 식별 정보가 없는 토큰은 여기서 정지 회원으로 판단하지 않음
        if (issuer == null || subject == null) {
            return new AuthorizationDecision(true);
        }

        boolean suspended = userService.isSuspendedUser(issuer.toString(), subject);
        return new AuthorizationDecision(!suspended);
    }
}
