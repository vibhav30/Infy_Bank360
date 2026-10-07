package com.infy.bank360.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscountCalculationResult {
    private BigDecimal grossAmount;
    private BigDecimal discount;
    private BigDecimal netAmount;
}
