package com.infy.bank360.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountSummaryDTO {
    private String accountNumber;
    private Long customerId;
    private String accountType;
    private BigDecimal balance;
    private String status;
}
