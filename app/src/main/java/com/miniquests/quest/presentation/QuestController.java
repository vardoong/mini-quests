package com.miniquests.quest.presentation;

import com.miniquests.quest.application.QuestCommandService;
import com.miniquests.quest.application.QuestQueryService;
import com.miniquests.quest.presentation.dto.MyQuestResponse;
import com.miniquests.quest.presentation.dto.QuestResponse;
import com.miniquests.user.application.UserService;
import com.miniquests.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URL;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/quests")
@RequiredArgsConstructor
public class QuestController {

    private final QuestQueryService questQueryService;
    private final UserService userService;
    private final QuestCommandService questCommandService;

    @GetMapping
    public List<QuestResponse> findActiveQuests() {
        return questQueryService.findActiveQuests().stream()
                .map(QuestResponse::from)
                .toList();
    }

    @GetMapping("/{questId}")
    public QuestResponse findActiveQuest(@PathVariable Long questId) {
        return questQueryService.findActiveQuest(questId)
                .map(QuestResponse::from)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "퀘스트를 찾을 수 없습니다.")
                );
    }

    @PostMapping("/{questId}/start")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void startQuest(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long questId
    ) {
        String displayName = jwt.getClaimAsString("name");
        if (displayName == null || displayName.isBlank()) {
            displayName = jwt.getClaimAsString("preferred_username");
        }

        URL issuer = jwt.getIssuer();
        String subject = jwt.getSubject();

        if (issuer == null || subject == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Token must contain iss and sub claims."
            );
        }

        User user = userService.getOrCreate(
                displayName,
                issuer.toString(),
                subject,
                jwt.getClaimAsString("email")
        );

        questCommandService.start(user.getId(), questId);
    }

    @PostMapping("/{questId}/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void completeQuest(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long questId
    ) {
        String displayName = jwt.getClaimAsString("name");
        if (displayName == null || displayName.isBlank()) {
            displayName = jwt.getClaimAsString("preferred_username");
        }

        URL issuer = jwt.getIssuer();
        String subject = jwt.getSubject();

        if (issuer == null || subject == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Token must contain iss and sub claims."
            );
        }

        User user = userService.getOrCreate(
                displayName,
                issuer.toString(),
                subject,
                jwt.getClaimAsString("email")
        );

        questCommandService.complete(user.getId(), questId, Instant.now());
    }

    @GetMapping("/my")
    public List<MyQuestResponse> findMyQuests(@AuthenticationPrincipal Jwt jwt) {
        String displayName = jwt.getClaimAsString("name");
        if (displayName == null || displayName.isBlank()) {
            displayName = jwt.getClaimAsString("preferred_username");
        }

        URL issuer = jwt.getIssuer();
        String subject = jwt.getSubject();

        if (issuer == null || subject == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Token must contain iss and sub claims."
            );
        }

        User user = userService.getOrCreate(
                displayName,
                issuer.toString(),
                subject,
                jwt.getClaimAsString("email")
        );

        return questQueryService.findMyQuests(user.getId()).stream()
                .map(MyQuestResponse::from)
                .toList();
    }
}
