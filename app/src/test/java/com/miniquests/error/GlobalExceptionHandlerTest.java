package com.miniquests.error;

import com.miniquests.common.error.BusinessException;
import com.miniquests.quest.error.QuestErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    @DisplayName("활성 퀘스트가 없으면 404 응답을 반환한다")
    void returnsNotFoundForActiveQuestNotFound() {
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessException(
                new BusinessException(QuestErrorCode.ACTIVE_QUEST_NOT_FOUND)
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(
                new ErrorResponse(
                        "QUE-001",
                        "활성 퀘스트를 찾을 수 없습니다.",
                        Map.of()
                ),
                response.getBody()
        );
    }

    @Test
    @DisplayName("이미 시작한 퀘스트면 409 응답을 반환한다")
    void returnsConflictForQuestAlreadyStarted() {
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessException(
                new BusinessException(QuestErrorCode.QUEST_ALREADY_STARTED)
        );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(
                new ErrorResponse(
                        "QUE-002",
                        "이미 시작한 퀘스트입니다.",
                        Map.of()
                ),
                response.getBody()
        );
    }

    @Test
    @DisplayName("사용자 퀘스트 진행 정보가 없으면 404 응답을 반환한다")
    void returnsNotFoundForUserQuestNotFound() {
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessException(
                new BusinessException(QuestErrorCode.USER_QUEST_NOT_FOUND)
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(
                new ErrorResponse(
                        "QUE-003",
                        "퀘스트를 찾을 수 없습니다.",
                        Map.of()
                ),
                response.getBody()
        );
    }
}
