package com.miniquests.error;

import com.miniquests.common.error.BusinessException;
import com.miniquests.common.error.ErrorCode;
import com.miniquests.common.error.ErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();

        log.warn("[{}] {} details={}", errorCode.code(), e.getMessage(), e.getDetails());

        return ResponseEntity
                .status(toStatus(errorCode.type()))
                .body(
                        new ErrorResponse(
                                errorCode.code(),
                                errorCode.message(),
                                e.getDetails()
                        )
                );
    }

    private HttpStatus toStatus(ErrorType errorType) {
        return switch (errorType) {
            case INVALID -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            // 누락 검출을 위해 default 사용 안함
        };
    }
}
