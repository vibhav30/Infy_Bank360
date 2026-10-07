package com.infy.bank360.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HighValueTransactionDTO {
    private Long flagId;
    private Long transactionId;
    private String sourceAccount;
    private String destinationAccount;
    private BigDecimal amount;
    private String flagReason;
    private LocalDateTime flaggedAt;
    private boolean reviewed;
}
