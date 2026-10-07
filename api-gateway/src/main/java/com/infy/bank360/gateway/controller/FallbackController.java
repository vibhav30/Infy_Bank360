package com.infy.bank360.gateway.controller;

import com.infy.bank360.common.dto.ErrorInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/service-unavailable")
    public ResponseEntity<ErrorInfo> handleServiceUnavailable() {
        ErrorInfo error = ErrorInfo.builder()
                .errorCode("ERR_SERVICE_UNAVAILABLE")
                .errorMessage("The requested downstream banking microservice is currently unavailable or circuit is open. Please try again later.")
                .httpStatusCode(HttpStatus.SERVICE_UNAVAILABLE.value())
                .timestamp(LocalDateTime.now())
                .path("/fallback/service-unavailable")
                .build();
        return new ResponseEntity<>(error, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
