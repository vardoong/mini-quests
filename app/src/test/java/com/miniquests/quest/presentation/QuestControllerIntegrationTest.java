package com.miniquests.quest.presentation;

import com.miniquests.quest.domain.Quest;
import com.miniquests.quest.repository.QuestRepository;
import com.miniquests.quest.domain.UserQuest;
import com.miniquests.quest.domain.UserQuestStatus;
import com.miniquests.quest.repository.UserQuestRepository;
import com.miniquests.user.domain.User;
import com.miniquests.user.domain.UserStatus;
import com.miniquests.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class QuestControllerIntegrationTest {

    private static final String OIDC_ISSUER = "http://127.0.0.1:8080/realms/mini-quests";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private QuestRepository questRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserQuestRepository userQuestRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("인증되지 않은 요청은 공통 인증 오류 응답을 반환한다")
    void rejectsUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/quests"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists(HttpHeaders.WWW_AUTHENTICATE))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("APP-002"))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    @DisplayName("활성 회원은 퀘스트 API에 접근할 수 있다")
    void allowsActiveUserToAccessQuestApi() throws Exception {
        String subject = UUID.randomUUID().toString();
        userRepository.saveAndFlush(User.builder()
                .displayName("활성 회원")
                .oidcIssuer(OIDC_ISSUER)
                .oidcSubject(subject)
                .status(UserStatus.ACTIVE)
                .build());

        mockMvc.perform(get("/api/quests")
                        .with(authenticatedJwt(subject, "활성 회원", "active@example.com")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("정지 회원은 퀘스트 목록 API에 접근할 수 없고 공통 403 응답을 받는다")
    void rejectsSuspendedUserFromQuestApi() throws Exception {
        String subject = UUID.randomUUID().toString();
        userRepository.saveAndFlush(User.builder()
                .displayName("정지 회원")
                .oidcIssuer(OIDC_ISSUER)
                .oidcSubject(subject)
                .status(UserStatus.SUSPENDED)
                .build());

        mockMvc.perform(get("/api/quests")
                        .with(authenticatedJwt(subject, "정지 회원", "suspended@example.com")))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("APP-008"))
                .andExpect(jsonPath("$.message").value("정지된 회원입니다."))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    @DisplayName("퀘스트 목록에는 활성 퀘스트만 포함된다")
    void returnsOnlyActiveQuests() throws Exception {
        String suffix = UUID.randomUUID().toString();
        String activeTitle = "활성 퀘스트 " + suffix;
        String inactiveTitle = "비활성 퀘스트 " + suffix;

        Quest activeQuest = Quest.builder()
                .title(activeTitle)
                .description("사용자에게 공개된 퀘스트")
                .build();
        activeQuest.active();

        Quest inactiveQuest = Quest.builder()
                .title(inactiveTitle)
                .description("사용자에게 공개되지 않은 퀘스트")
                .build();

        questRepository.save(activeQuest);
        questRepository.save(inactiveQuest);
        questRepository.flush();

        mockMvc.perform(get("/api/quests").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title").value(hasItem(activeTitle)))
                .andExpect(jsonPath("$[*].title").value(not(hasItem(inactiveTitle))));
    }

    @Test
    @DisplayName("활성 퀘스트 상세 정보를 조회할 수 있다")
    void returnsActiveQuestDetails() throws Exception {
        Quest quest = Quest.builder()
                .title("상세 조회 퀘스트")
                .description("활성 퀘스트 상세 정보")
                .build();
        quest.active();
        Quest savedQuest = questRepository.save(quest);
        questRepository.flush();

        mockMvc.perform(get("/api/quests/{questId}", savedQuest.getId()).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedQuest.getId()))
                .andExpect(jsonPath("$.title").value("상세 조회 퀘스트"))
                .andExpect(jsonPath("$.description").value("활성 퀘스트 상세 정보"));
    }

    @Test
    @DisplayName("비활성 퀘스트 상세 조회는 404를 반환한다")
    void returnsNotFoundForInactiveQuest() throws Exception {
        Quest quest = questRepository.save(
                Quest.builder()
                        .title("비활성 퀘스트")
                        .description("조회할 수 없는 퀘스트")
                        .build()
        );
        questRepository.flush();

        mockMvc.perform(get("/api/quests/{questId}", quest.getId()).with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("QUE-001"))
                .andExpect(jsonPath("$.message").value("활성 퀘스트를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("인증되지 않은 요청은 퀘스트를 시작할 수 없다")
    void rejectsUnauthenticatedQuestStart() throws Exception {
        mockMvc.perform(post("/api/quests/{questId}/start", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("인증된 사용자가 활성 퀘스트를 시작하면 진행 정보가 저장된다")
    void startsActiveQuestForAuthenticatedUser() throws Exception {
        String subject = UUID.randomUUID().toString();

        Quest quest = Quest.builder()
                .title("시작 API 테스트 퀘스트")
                .description("사용자가 시작할 수 있는 퀘스트")
                .build();
        quest.active();
        Quest savedQuest = questRepository.saveAndFlush(quest);

        mockMvc.perform(post("/api/quests/{questId}/start", savedQuest.getId())
                        .with(authenticatedJwt(subject, "테스트 회원", "test@example.com")))
                .andExpect(status().isNoContent());

        User user = userRepository.findByOidcIssuerAndOidcSubject(OIDC_ISSUER, subject)
                .orElseThrow();
        UserQuest userQuest = userQuestRepository
                .findByUserIdAndQuest_Id(user.getId(), savedQuest.getId())
                .orElseThrow();

        assertEquals(UserQuestStatus.IN_PROGRESS, userQuest.getStatus());
        assertNotNull(userQuest.getStartedAt());
        assertEquals(savedQuest.getId(), userQuest.getQuest().getId());
    }

    @Test
    @DisplayName("인증되지 않은 요청은 퀘스트를 완료할 수 없다")
    void rejectsUnauthenticatedQuestCompletion() throws Exception {
        mockMvc.perform(post("/api/quests/{questId}/complete", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("인증된 사용자가 시작한 퀘스트를 완료하면 완료 정보가 기록된다")
    void completesStartedQuestForAuthenticatedUser() throws Exception {
        String subject = UUID.randomUUID().toString();
        RequestPostProcessor authentication = authenticatedJwt(
                subject,
                "테스트 회원",
                "test@example.com"
        );

        Quest quest = Quest.builder()
                .title("완료 API 테스트 퀘스트")
                .description("완료할 퀘스트")
                .build();
        quest.active();
        Quest savedQuest = questRepository.saveAndFlush(quest);

        mockMvc.perform(post("/api/quests/{questId}/start", savedQuest.getId())
                        .with(authentication))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/quests/{questId}/complete", savedQuest.getId())
                        .with(authentication))
                .andExpect(status().isNoContent());

        User user = userRepository.findByOidcIssuerAndOidcSubject(OIDC_ISSUER, subject)
                .orElseThrow();
        UserQuest userQuest = userQuestRepository
                .findByUserIdAndQuest_Id(user.getId(), savedQuest.getId())
                .orElseThrow();

        assertEquals(UserQuestStatus.COMPLETED, userQuest.getStatus());
        assertNotNull(userQuest.getCompletedAt());
        assertEquals(savedQuest.getId(), userQuest.getQuest().getId());
    }

    @Test
    @DisplayName("인증되지 않은 요청은 내 퀘스트 목록을 조회할 수 없다")
    void rejectsUnauthenticatedMyQuestRequest() throws Exception {
        mockMvc.perform(get("/api/quests/my"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("내 퀘스트 목록은 해당 회원의 퀘스트만 시작 시각 내림차순으로 반환한다")
    void returnsMyQuestsInStartedAtDescendingOrder() throws Exception {
        String mySubject = UUID.randomUUID().toString();
        String anotherSubject = UUID.randomUUID().toString();

        Quest olderQuest = saveActiveQuest("내 오래된 퀘스트");
        Quest newerQuest = saveActiveQuest("내 최근 퀘스트");
        Quest anotherUsersQuest = saveActiveQuest("다른 회원의 퀘스트");

        startQuest(mySubject, olderQuest);
        startQuest(mySubject, newerQuest);
        startQuest(anotherSubject, anotherUsersQuest);

        mockMvc.perform(post("/api/quests/{questId}/complete", newerQuest.getId())
                        .with(authenticatedJwt(mySubject, "테스트 회원", "test@example.com")))
                .andExpect(status().isNoContent());

        User myUser = userRepository.findByOidcIssuerAndOidcSubject(OIDC_ISSUER, mySubject)
                .orElseThrow();
        UserQuest olderUserQuest = userQuestRepository
                .findByUserIdAndQuest_Id(myUser.getId(), olderQuest.getId())
                .orElseThrow();
        UserQuest newerUserQuest = userQuestRepository
                .findByUserIdAndQuest_Id(myUser.getId(), newerQuest.getId())
                .orElseThrow();

        jdbcTemplate.update(
                "UPDATE user_quests SET started_at = ? WHERE id = ?",
                Timestamp.from(Instant.parse("2026-11-24T10:00:00Z")),
                olderUserQuest.getId()
        );
        jdbcTemplate.update(
                "UPDATE user_quests SET started_at = ? WHERE id = ?",
                Timestamp.from(Instant.parse("2026-11-25T10:00:00Z")),
                newerUserQuest.getId()
        );

        mockMvc.perform(get("/api/quests/my")
                        .with(authenticatedJwt(mySubject, "테스트 회원", "test@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].questId").value(newerQuest.getId()))
                .andExpect(jsonPath("$[0].title").value("내 최근 퀘스트"))
                .andExpect(jsonPath("$[0].userQuestStatus").value("COMPLETED"))
                .andExpect(jsonPath("$[0].startedAt").isNotEmpty())
                .andExpect(jsonPath("$[0].completedAt").isNotEmpty())
                .andExpect(jsonPath("$[1].questId").value(olderQuest.getId()))
                .andExpect(jsonPath("$[*].questId").value(not(hasItem(anotherUsersQuest.getId()))));
    }

    @Test
    @DisplayName("내 퀘스트 진행 정보가 없으면 빈 목록을 반환한다")
    void returnsEmptyListWhenIHaveNoQuests() throws Exception {
        String subject = UUID.randomUUID().toString();

        mockMvc.perform(get("/api/quests/my")
                        .with(authenticatedJwt(subject, "테스트 회원", "test@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    private Quest saveActiveQuest(String title) {
        Quest quest = Quest.builder()
                .title(title)
                .description("내 퀘스트 목록 API 테스트")
                .build();
        quest.active();
        return questRepository.saveAndFlush(quest);
    }

    private void startQuest(String subject, Quest quest) throws Exception {
        mockMvc.perform(post("/api/quests/{questId}/start", quest.getId())
                        .with(authenticatedJwt(subject, "테스트 회원", "test@example.com")))
                .andExpect(status().isNoContent());
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
