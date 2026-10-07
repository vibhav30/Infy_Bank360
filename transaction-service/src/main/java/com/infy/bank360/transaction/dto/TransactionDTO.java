package com.infy.bank360.transaction.dto;

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
public class TransactionDTO {
    private Long transactionId;
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private BigDecimal amount;
    private BigDecimal discount;
    private BigDecimal netAmount;
    private String transactionType;
    private LocalDateTime transactionDate;
    private String status;
    private String referenceNumber;
    private String billerCategory;
}
