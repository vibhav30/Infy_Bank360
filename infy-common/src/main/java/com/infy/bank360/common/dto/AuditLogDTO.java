package com.infy.bank360.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {
    private Long auditId;
    private String userId;
    private String userRole;
    private String action;
    private String resourceId;
    private String details;
    private String clientIpAddress;
    private LocalDateTime timestamp;
}
