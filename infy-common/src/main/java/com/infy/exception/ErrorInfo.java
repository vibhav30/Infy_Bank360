package com.infy.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard enterprise error structure for Infy_Bank360 platform.
 * Reference: spec.md & plan.md (Section 2.2)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorInfo {
    private String errorCode;
    private String errorMessage;
    private LocalDateTime timestamp;
}
