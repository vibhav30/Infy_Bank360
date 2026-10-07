package com.infy.utility;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard ErrorInfo envelope under com.infy.utility.
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
