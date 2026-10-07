package com.infy.bank360.audit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auditId;

    @Column(nullable = false, length = 60)
    private String userId;

    @Column(nullable = false, length = 30)
    private String userRole;

    @Column(nullable = false, length = 50)
    private String action; // LOGIN, KYC_VERIFICATION, ACCOUNT_STATE_CHANGE, FUND_TRANSFER, LOAN_APPROVAL

    @Column(length = 60)
    private String resourceId;

    @Column(length = 1000)
    private String details;

    @Column(length = 45)
    private String clientIpAddress;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}
