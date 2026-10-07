package com.infy.bank360.common.exception;

import com.infy.bank360.common.dto.ErrorInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Centralized exception handler for all Infy_Bank360 microservices.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InfyBankException.class)
    public ResponseEntity<ErrorInfo> handleInfyBankException(InfyBankException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if ("ERR_ACCOUNT_FROZEN".equalsIgnoreCase(ex.getErrorCode())) {
            status = HttpStatus.FORBIDDEN;
        } else if ("ERR_USER_NOT_FOUND".equalsIgnoreCase(ex.getErrorCode()) || "ERR_ACCOUNT_NOT_FOUND".equalsIgnoreCase(ex.getErrorCode())) {
            status = HttpStatus.NOT_FOUND;
        } else if ("ERR_USER_EXISTS".equalsIgnoreCase(ex.getErrorCode())) {
            status = HttpStatus.CONFLICT;
        }

        ErrorInfo errorInfo = ErrorInfo.builder()
                .errorCode(ex.getErrorCode())
                .errorMessage(ex.getMessage())
                .httpStatusCode(status.value())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(errorInfo, status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorInfo> handleValidationException(MethodArgumentNotValidException ex) {
        String combinedMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ErrorInfo errorInfo = ErrorInfo.builder()
                .errorCode("ERR_INVALID_INPUT")
                .errorMessage(combinedMessage)
                .httpStatusCode(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(errorInfo, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorInfo> handleGeneralException(Exception ex) {
        ErrorInfo errorInfo = ErrorInfo.builder()
                .errorCode("ERR_INTERNAL_SERVER")
                .errorMessage(ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred.")
                .httpStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(errorInfo, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
