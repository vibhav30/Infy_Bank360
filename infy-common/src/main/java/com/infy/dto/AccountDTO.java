package com.infy.dto;

import com.infy.entity.Account;
import com.infy.entity.Customer;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object: AccountDTO
 * Maps to Account entity with strict JSR-380 bean validations.
 * Reference: spec.md (AC-ACCT-01) & plan.md (Section 2.1.2)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {

    private Long accountId;

    @Pattern(regexp = "^[0-9]{12}$", message = "Account number must be exactly 12 digits")
    private String accountNumber;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotBlank(message = "Account type is required")
    @Pattern(regexp = "^(SAVINGS|CHECKING)$", message = "Account type must be SAVINGS or CHECKING")
    private String accountType;

    @NotNull(message = "Opening balance is required")
    @DecimalMin(value = "0.00", message = "Balance cannot be negative")
    private BigDecimal balance;

    @Pattern(regexp = "^(ACTIVE|FROZEN|CLOSED)$", message = "Status must be ACTIVE, FROZEN, or CLOSED")
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Converts an Account JPA Entity to AccountDTO.
     */
    public static AccountDTO entityToDTO(Account entity) {
        if (entity == null) {
            return null;
        }
        return AccountDTO.builder()
                .accountId(entity.getAccountId())
                .accountNumber(entity.getAccountNumber())
                .customerId(entity.getCustomer() != null ? entity.getCustomer().getCustomerId() : null)
                .accountType(entity.getAccountType())
                .balance(entity.getBalance())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Converts an AccountDTO to Account JPA Entity.
     */
    public static Account dtoToEntity(AccountDTO dto) {
        if (dto == null) {
            return null;
        }
        Customer customerStub = null;
        if (dto.getCustomerId() != null) {
            customerStub = Customer.builder().customerId(dto.getCustomerId()).build();
        }

        return Account.builder()
                .accountId(dto.getAccountId())
                .accountNumber(dto.getAccountNumber())
                .customer(customerStub)
                .accountType(dto.getAccountType())
                .balance(dto.getBalance() != null ? dto.getBalance() : BigDecimal.ZERO)
                .status(dto.getStatus() != null ? dto.getStatus() : "ACTIVE")
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}
