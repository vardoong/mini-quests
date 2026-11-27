package com.miniquests.user.presentation;

import com.miniquests.user.domain.User;
import com.miniquests.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerIntegrationTest {

    private static final String OIDC_ISSUER = "http://127.0.0.1:8080/realms/mini-quests";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("인증되지 않은 요청은 현재 회원 정보를 조회할 수 없다")
    void rejectsUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("iss 클레임이 없는 인증 토큰은 공통 401 오류 응답을 반환한다")
    void rejectsAuthenticatedJwtWithoutIssuerClaim() throws Exception {
        long userCountBeforeRequest = userRepository.count();

        mockMvc.perform(get("/api/users/me")
                        .with(jwt().jwt(token -> token.subject("subject-without-issuer"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("APP-001"))
                .andExpect(jsonPath("$.message").value("인증 토큰에 필수 사용자 정보가 없습니다."));

        assertEquals(userCountBeforeRequest, userRepository.count());
    }

    @Test
    @DisplayName("인증된 JWT 정보로 회원을 생성하고 현재 회원 정보를 반환한다")
    void createsUserFromAuthenticatedJwt() throws Exception {
        String subject = UUID.randomUUID().toString();

        mockMvc.perform(
                        get("/api/users/me")
                                .with(authenticatedJwt(subject, "테스트 회원", "test@example.com"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.displayName").value("테스트 회원"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        User createdUser = userRepository.findByOidcIssuerAndOidcSubject(OIDC_ISSUER, subject)
                .orElseThrow();

        assertNotNull(createdUser.getId());
        assertEquals("테스트 회원", createdUser.getDisplayName());
        assertEquals("test@example.com", createdUser.getEmail());
    }

    @Test
    @DisplayName("같은 OIDC 사용자로 다시 요청하면 기존 회원을 반환한다")
    void returnsExistingUserForSameOidcIdentity() throws Exception {
        String subject = UUID.randomUUID().toString();

        mockMvc.perform(get("/api/users/me")
                        .with(authenticatedJwt(subject, "처음 이름", "first@example.com")))
                .andExpect(status().isOk());

        User firstUser = userRepository.findByOidcIssuerAndOidcSubject(OIDC_ISSUER, subject)
                .orElseThrow();

        mockMvc.perform(get("/api/users/me")
                        .with(authenticatedJwt(subject, "변경된 이름", "changed@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(firstUser.getId()))
                .andExpect(jsonPath("$.displayName").value("처음 이름"))
                .andExpect(jsonPath("$.email").value("first@example.com"));

        User repeatedUser = userRepository.findByOidcIssuerAndOidcSubject(OIDC_ISSUER, subject)
                .orElseThrow();
        assertEquals(firstUser.getId(), repeatedUser.getId());
    }

    private RequestPostProcessor authenticatedJwt(
            String subject,
            String displayName,
            String email
    ) {
        return jwt().jwt(token -> token
                .claim("iss", OIDC_ISSUER)
                .subject(subject)
                .claim("name", displayName)
                .claim("email", email));
    }
}
