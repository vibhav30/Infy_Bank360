package com.infy.exception;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Centralized REST Exception Advice for all Infy_Bank360 microservices.
 * Reference: spec.md & plan.md (Section 2.2)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Environment environment;

    /**
     * Strict Constructor Injection for Spring Environment.
     */
    public GlobalExceptionHandler(Environment environment) {
        this.environment = environment;
    }

    /**
     * Intercepts InfyBankException and maps to HTTP 400 Bad Request.
     */
    @ExceptionHandler(InfyBankException.class)
    public ResponseEntity<ErrorInfo> handleInfyBankException(InfyBankException ex) {
        String defaultCode = "ERR_BUSINESS_VALIDATION";
        String errorCode = ex.getErrorCode() != null ? ex.getErrorCode() : defaultCode;

        // Optionally read mapped property message from environment
        String envMessage = environment.getProperty(errorCode);
        String errorMessage = (envMessage != null && !envMessage.isBlank()) ? envMessage : ex.getMessage();

        ErrorInfo errorInfo = ErrorInfo.builder()
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(errorInfo, HttpStatus.BAD_REQUEST);
    }

    /**
     * Intercepts MethodArgumentNotValidException to parse field validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorInfo> handleValidationException(MethodArgumentNotValidException ex) {
        String validationErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        ErrorInfo errorInfo = ErrorInfo.builder()
                .errorCode("ERR_METHOD_ARGUMENT_NOT_VALID")
                .errorMessage(validationErrors)
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(errorInfo, HttpStatus.BAD_REQUEST);
    }

    /**
     * Intercepts unexpected runtime exceptions and maps to HTTP 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorInfo> handleGeneralException(Exception ex) {
        ErrorInfo errorInfo = ErrorInfo.builder()
                .errorCode("ERR_INTERNAL_SERVER_ERROR")
                .errorMessage(ex.getMessage() != null ? ex.getMessage() : "An unexpected server error occurred.")
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(errorInfo, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
