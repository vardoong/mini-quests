package com.miniquests.error;

import com.miniquests.common.error.BusinessException;
import com.miniquests.common.error.ErrorCode;
import com.miniquests.common.error.ErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.core.JacksonException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String INVALID_FORMAT_MESSAGE = "데이터 형식이 올바르지 않습니다.";
    private static final String INVALID_VALUE_MESSAGE = "유효하지 않은 값입니다.";

    // 공통 비즈니스 예외
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();

        if (toStatus(errorCode.type()).is5xxServerError()) {
            log.error("[{}] {} details={}", errorCode.code(), e.getMessage(), e.getDetails(), e);
        } else {
            log.warn("[{}] {} details={}", errorCode.code(), e.getMessage(), e.getDetails());
        }

        return toResponseEntity(errorCode, e.getDetails());
    }

    // 데이터 유효성 오류
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e
    ) {
        List<Map<String, String>> fieldErrors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> fieldError(error.getField(), toMessage(error)))
                .toList();

        return invalidRequest(fieldErrors);
    }

    // Request body 형식 오류
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e
    ) {
        List<Map<String, String>> fieldErrors = List.of();

        if (e.getCause() instanceof JacksonException ex) {
            String field = ex.getPath().stream()
                    .map(JacksonException.Reference::getPropertyName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("."));

            if (!field.isBlank()) {
                fieldErrors = List.of(fieldError(field, INVALID_FORMAT_MESSAGE));
            }
        }

        return invalidRequest(fieldErrors);
    }

    // Request param, path variable 타입 오류
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e
    ) {
        return invalidRequest(List.of(fieldError(e.getName(), INVALID_FORMAT_MESSAGE)));
    }

    // 존재하지 않는 경로 (404)
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleEndpointNotFound(
            NoResourceFoundException e
    ) {
        AppErrorCode errorCode = AppErrorCode.ENDPOINT_NOT_FOUND;

        logClientWarn(errorCode, e.getMessage());

        return toResponseEntity(errorCode, null);
    }

    // 지원하지 않는 HTTP 메서드 (405)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException e
    ) {
        AppErrorCode errorCode = AppErrorCode.METHOD_NOT_ALLOWED;

        logClientWarn(errorCode, e.getMessage());

        return ResponseEntity
                .status(toStatus(errorCode.type()))
                .headers(e.getHeaders()) // 허용 메서드를 알려주는 Allow 헤더 유지
                .body(new ErrorResponse(errorCode.code(), errorCode.message()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception e
    ) {

        // Spring 표준 4xx 예외는 원래 상태 코드와 헤더 유지
        if (e instanceof org.springframework.web.ErrorResponse springError
                && springError.getStatusCode().is4xxClientError()) {

            AppErrorCode errorCode = AppErrorCode.UNCLASSIFIED_CLIENT_ERROR;

            log.warn(
                    "[{}] status={} title={} exception={}",
                    errorCode.code(),
                    springError.getStatusCode().value(),
                    springError.getBody().getTitle(),
                    e.getClass().getSimpleName()
            );

            return ResponseEntity
                    .status(springError.getStatusCode())
                    .headers(springError.getHeaders())
                    .body(new ErrorResponse(
                            errorCode.code(),
                            errorCode.message()
                    ));
        }

        log.error("처리되지 않은 예외가 발생했습니다.", e);

        return toResponseEntity(AppErrorCode.INTERNAL_SERVER_ERROR, null);
    }

    private ResponseEntity<ErrorResponse> invalidRequest(List<Map<String, String>> fieldErrors) {
        AppErrorCode errorCode = AppErrorCode.INVALID_REQUEST;

        log.warn("[{}] fields={}", errorCode.code(),
                fieldErrors.stream().map(error -> error.get("field")).toList());

        Map<String, Object> details = fieldErrors.isEmpty()
                ? null
                : Map.of("fieldErrors", fieldErrors);

        return toResponseEntity(errorCode, details);
    }


    private ResponseEntity<ErrorResponse> toResponseEntity(
            ErrorCode errorCode,
            Map<String, Object> details
    ) {
        return ResponseEntity
                .status(toStatus(errorCode.type()))
                .body(new ErrorResponse(errorCode.code(), errorCode.message(), details));
    }

    // 타입 변환 실패 메시지는 내부 클래스명이 포함되므로 대체
    private String toMessage(FieldError error) {
        return error.isBindingFailure()
                ? INVALID_FORMAT_MESSAGE
                : Objects.requireNonNullElse(error.getDefaultMessage(), INVALID_VALUE_MESSAGE);
    }

    private Map<String, String> fieldError(String field, String message) {
        return Map.of("field", field, "message", message);
    }

    private void logClientWarn(ErrorCode errorCode, String detail) {
        log.warn("[{}] {}", errorCode.code(), detail);
    }

    private HttpStatus toStatus(ErrorType errorType) {
        return switch (errorType) {
            case INVALID -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case INTERNAL_SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
            case METHOD_NOT_ALLOWED -> HttpStatus.METHOD_NOT_ALLOWED;
            // 누락 검출을 위해 default 사용 안함
        };
    }
}
