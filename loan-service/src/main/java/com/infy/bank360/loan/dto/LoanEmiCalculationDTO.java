package com.infy.bank360.loan.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanEmiCalculationDTO {

    @NotNull(message = "Principal amount is required")
    @DecimalMin(value = "1000.00", message = "Minimum principal is 1000.00")
    private BigDecimal principalAmount;

    @NotNull(message = "Tenure is required")
    @Min(value = 12, message = "Minimum tenure is 12 months")
    @Max(value = 120, message = "Maximum tenure is 120 months")
    private Integer tenureMonths;

    private String loanType; // PERSONAL, HOME, VEHICLE
    private Double annualInterestRate;
    private BigDecimal monthlyEmi;
    private BigDecimal totalRepayment;
    private BigDecimal totalInterest;
}
