package com.miniquests.quest.presentation;

import com.miniquests.common.error.BusinessException;
import com.miniquests.quest.application.QuestCommandService;
import com.miniquests.quest.application.QuestQueryService;
import com.miniquests.quest.error.QuestErrorCode;
import com.miniquests.quest.presentation.dto.MyQuestResponse;
import com.miniquests.quest.presentation.dto.QuestResponse;
import com.miniquests.security.JwtUserResolver;
import com.miniquests.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/quests")
@RequiredArgsConstructor
public class QuestController {

    private final QuestQueryService questQueryService;
    private final QuestCommandService questCommandService;
    private final JwtUserResolver jwtUserResolver;

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
                        new BusinessException(QuestErrorCode.ACTIVE_QUEST_NOT_FOUND)
                );
    }

    @PostMapping("/{questId}/start")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void startQuest(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long questId
    ) {
        User user = jwtUserResolver.resolve(jwt);
        questCommandService.start(user.getId(), questId);
    }

    @PostMapping("/{questId}/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void completeQuest(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long questId
    ) {
        User user = jwtUserResolver.resolve(jwt);
        questCommandService.complete(user.getId(), questId, Instant.now());
    }

    @GetMapping("/my")
    public List<MyQuestResponse> findMyQuests(@AuthenticationPrincipal Jwt jwt) {
        User user = jwtUserResolver.resolve(jwt);
        return questQueryService.findMyQuests(user.getId()).stream()
                .map(MyQuestResponse::from)
                .toList();
    }
}
