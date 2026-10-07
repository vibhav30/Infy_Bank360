package com.infy.bank360.account.dto;

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
public class AccountCreateDTO {

    @NotNull(message = "Customer ID is required")
    @Positive(message = "Customer ID must be a positive number")
    private Long customerId;

    @NotBlank(message = "Account type is required")
    @Pattern(regexp = "^(SAVINGS|CHECKING)$", message = "Account type must be SAVINGS or CHECKING")
    private String accountType;

    @NotNull(message = "Initial deposit is required")
    @DecimalMin(value = "100.00", message = "Minimum opening deposit must be at least 100.00")
    private BigDecimal initialDeposit;
}
