package com.infy.dto;

import com.infy.entity.Customer;
import com.infy.entity.LoanApplication;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object: LoanApplicationDTO
 * Maps to LoanApplication entity with strict JSR-380 bean validations.
 * Reference: spec.md (AC-LOAN-01 to AC-LOAN-03) & plan.md (Section 2.1.4)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationDTO {

    private Long loanId;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

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

    @NotNull(message = "Annual interest rate is mandatory")
    @DecimalMin(value = "0.01", message = "Annual interest rate must be greater than zero")
    private BigDecimal annualInterestRate;

    @NotNull(message = "Monthly EMI is mandatory")
    @DecimalMin(value = "0.01", message = "Monthly EMI must be greater than zero")
    private BigDecimal monthlyEmi;

    @NotNull(message = "Monthly income is mandatory")
    @DecimalMin(value = "500.00", message = "Monthly declared income must be at least 500.00")
    private BigDecimal monthlyIncome;

    @Pattern(regexp = "^(APPLIED|APPROVED|REJECTED|DISBURSED)$", message = "Status must be APPLIED, APPROVED, REJECTED, or DISBURSED")
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Converts a LoanApplication JPA Entity to LoanApplicationDTO.
     */
    public static LoanApplicationDTO entityToDTO(LoanApplication entity) {
        if (entity == null) {
            return null;
        }
        return LoanApplicationDTO.builder()
                .loanId(entity.getLoanId())
                .customerId(entity.getCustomer() != null ? entity.getCustomer().getCustomerId() : null)
                .loanType(entity.getLoanType())
                .principalAmount(entity.getPrincipalAmount())
                .tenureMonths(entity.getTenureMonths())
                .annualInterestRate(entity.getAnnualInterestRate())
                .monthlyEmi(entity.getMonthlyEmi())
                .monthlyIncome(entity.getMonthlyIncome())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Converts a LoanApplicationDTO to LoanApplication JPA Entity.
     */
    public static LoanApplication dtoToEntity(LoanApplicationDTO dto) {
        if (dto == null) {
            return null;
        }

        Customer customerStub = null;
        if (dto.getCustomerId() != null) {
            customerStub = Customer.builder().customerId(dto.getCustomerId()).build();
        }

        return LoanApplication.builder()
                .loanId(dto.getLoanId())
                .customer(customerStub)
                .loanType(dto.getLoanType())
                .principalAmount(dto.getPrincipalAmount())
                .tenureMonths(dto.getTenureMonths())
                .annualInterestRate(dto.getAnnualInterestRate())
                .monthlyEmi(dto.getMonthlyEmi())
                .monthlyIncome(dto.getMonthlyIncome())
                .status(dto.getStatus() != null ? dto.getStatus() : "APPLIED")
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}
