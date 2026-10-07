package com.infy.bank360.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard enterprise error structure for Infy_Bank360 platform.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorInfo {
    private String errorCode;
    private String errorMessage;
    private Integer httpStatusCode;
    private LocalDateTime timestamp;
    private String path;
}
