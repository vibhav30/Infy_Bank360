package com.infy.bank360.gateway.fallback;

import com.infy.bank360.common.dto.ErrorInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import java.time.LocalDateTime;

/**
 * Handles fallback responses when downstream microservices trip the Resilience4j CircuitBreaker.
 */
@RestController
@RequestMapping("/fallback")
public class GatewayFallbackController {

    @GetMapping("/service-unavailable")
    public ResponseEntity<ErrorInfo> handleGetFallback(ServerWebExchange exchange) {
        return buildFallbackResponse(exchange);
    }

    @PostMapping("/service-unavailable")
    public ResponseEntity<ErrorInfo> handlePostFallback(ServerWebExchange exchange) {
        return buildFallbackResponse(exchange);
    }

    private ResponseEntity<ErrorInfo> buildFallbackResponse(ServerWebExchange exchange) {
        String path = exchange.getRequest().getURI().getPath();
        ErrorInfo errorInfo = ErrorInfo.builder()
                .errorCode("ERR_SERVICE_TEMPORARILY_UNAVAILABLE")
                .errorMessage("The requested banking service is temporarily unavailable or degraded. Please retry shortly.")
                .httpStatusCode(HttpStatus.SERVICE_UNAVAILABLE.value())
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorInfo);
    }
}
