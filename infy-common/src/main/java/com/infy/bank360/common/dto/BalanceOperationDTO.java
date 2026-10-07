package com.infy.bank360.common.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceOperationDTO {
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be strictly greater than zero")
    private BigDecimal amount;
    private String remarks;
    private String transactionReference;
}
