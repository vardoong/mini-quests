package com.miniquests.error;

import com.miniquests.common.error.BusinessException;
import com.miniquests.quest.error.QuestErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

    @Test
    @DisplayName("필드 유효성 오류는 필드 정보와 함께 400 응답을 반환한다")
    void returnsBadRequestWithFieldErrorDetails() {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError(
                "request",
                "questId",
                "invalid",
                false,
                null,
                null,
                "퀘스트 ID가 유효하지 않습니다."
        ));
        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ErrorResponse> response =
                globalExceptionHandler.handleMethodArgumentNotValidException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(
                new ErrorResponse(
                        "APP-003",
                        "요청 값이 유효하지 않습니다.",
                        Map.of("fieldErrors", List.of(Map.of(
                                "field", "questId",
                                "message", "퀘스트 ID가 유효하지 않습니다."
                        )))
                ),
                response.getBody()
        );
    }

    @Test
    @DisplayName("Request body 형식 오류는 필드 정보 없이 400 응답을 반환한다")
    void returnsBadRequestForUnreadableRequestBody() {
        HttpMessageNotReadableException exception =
                new HttpMessageNotReadableException("Invalid request body", null);

        ResponseEntity<ErrorResponse> response =
                globalExceptionHandler.handleHttpMessageNotReadableException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(
                new ErrorResponse("APP-003", "요청 값이 유효하지 않습니다."),
                response.getBody()
        );
    }

    @Test
    @DisplayName("Spring 404 예외는 상태 코드와 앱 공통 요청 오류 코드를 반환한다")
    void preservesNotFoundStatusForSpringClientError() {
        NoResourceFoundException exception =
                new NoResourceFoundException(HttpMethod.GET, "/missing", "/missing");

        ResponseEntity<ErrorResponse> response =
                globalExceptionHandler.handleEndpointNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(
                new ErrorResponse("APP-005", "요청한 경로를 찾을 수 없습니다."),
                response.getBody()
        );
    }

    @Test
    @DisplayName("Spring 405 예외는 상태 코드와 Allow 헤더를 보존한다")
    void preservesMethodNotAllowedStatusAndAllowHeader() {
        HttpRequestMethodNotSupportedException exception =
                new HttpRequestMethodNotSupportedException("PATCH", List.of("GET", "POST"));

        ResponseEntity<ErrorResponse> response =
                globalExceptionHandler.handleMethodNotAllowed(exception);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertEquals(Set.of(HttpMethod.GET, HttpMethod.POST), response.getHeaders().getAllow());
        assertEquals(
                new ErrorResponse("APP-006", "지원하지 않는 HTTP 메서드입니다."),
                response.getBody()
        );
    }

    @Test
    @DisplayName("분류되지 않은 Spring 4xx는 원래 상태와 공통 오류 응답을 반환한다")
    void preservesStatusAndHidesSpringErrorDetailsForUnclassifiedClientError() {
        ResponseStatusException exception = new ResponseStatusException(
                HttpStatus.I_AM_A_TEAPOT,
                "internal spring detail"
        );

        ResponseEntity<ErrorResponse> response =
                globalExceptionHandler.handleException(exception);

        assertEquals(HttpStatus.I_AM_A_TEAPOT, response.getStatusCode());
        assertEquals(
                new ErrorResponse("APP-007", "요청을 처리할 수 없습니다."),
                response.getBody()
        );
    }

    @Test
    @DisplayName("처리되지 않은 예외면 일반 서버 오류 코드와 500을 반환한다")
    void returnsInternalServerErrorForUnhandledException() {
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleException(
                new IllegalStateException("unexpected failure")
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(
                new ErrorResponse("APP-004", "서버 내부 오류가 발생했습니다."),
                response.getBody()
        );
    }
}
