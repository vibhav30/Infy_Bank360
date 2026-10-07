package com.infy.bank360.loan.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationDTO {

    private Long loanId;

    @NotNull(message = "Customer ID is required")
    @Positive(message = "Customer ID must be a positive number")
    private Long customerId;

    private String accountNumber;

    @NotBlank(message = "Loan type is mandatory")
    @Pattern(regexp = "^(PERSONAL|HOME|VEHICLE)$", message = "Loan type must be PERSONAL, HOME, or VEHICLE")
    private String loanType;

    @NotNull(message = "Principal amount is required")
    @DecimalMin(value = "1000.00", message = "Minimum loan principal is 1000.00")
    private BigDecimal principalAmount;

    @NotNull(message = "Tenure is required")
    @Min(value = 12, message = "Minimum tenure is 12 months")
    @Max(value = 120, message = "Maximum tenure is 120 months")
    private Integer tenureMonths;

    private Double annualInterestRate;
    private BigDecimal monthlyEmi;

    @NotNull(message = "Monthly income is mandatory")
    @DecimalMin(value = "500.00", message = "Monthly declared income must be at least 500.00")
    private BigDecimal monthlyIncome;

    private String status; // APPLIED, APPROVED, REJECTED, DISBURSED
    private String rejectionRemarks;
    private String dtiWarning;
}
