package com.infy.bank360.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
public class BillPaymentRequestDTO {

    @NotBlank(message = "Source account number is required")
    @Pattern(regexp = "^[0-9]{12}$", message = "Source account must be an exact 12-digit number")
    private String accountNumber;

    @NotBlank(message = "Biller category is mandatory")
    @Pattern(regexp = "^(ELECTRICITY|MOBILE_POSTPAID|WATER|INTERNET)$",
            message = "Biller category must be ELECTRICITY, MOBILE_POSTPAID, WATER, or INTERNET")
    private String billerCategory;

    @NotBlank(message = "Bill reference number is required")
    private String billReferenceNumber;

    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Transaction amount must be strictly greater than zero")
    private BigDecimal amount;
}
