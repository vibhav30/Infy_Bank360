package com.infy.bank360.audit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "high_value_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HighValueTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long flagId;

    private Long transactionId;

    @Column(nullable = false, length = 12)
    private String sourceAccount;

    @Column(length = 12)
    private String destinationAccount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(length = 255)
    private String flagReason;

    @Column(nullable = false)
    private LocalDateTime flaggedAt;

    @Column(nullable = false)
    private boolean reviewed;

    @Column(length = 500)
    private String complianceNotes;
}
