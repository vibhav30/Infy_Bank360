package com.infy.dto;

import com.infy.entity.Account;
import com.infy.entity.TransactionRecord;
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
 * Data Transfer Object: TransactionRecordDTO
 * Maps to TransactionRecord entity with strict JSR-380 bean validations.
 * Reference: spec.md (AC-TXN-01 to AC-TXN-04) & plan.md (Section 2.1.3)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRecordDTO {

    private Long transactionId;

    @NotBlank(message = "Source account number is required")
    @Pattern(regexp = "^[0-9]{12}$", message = "Source account number must be exactly 12 digits")
    private String sourceAccountNumber;

    @Pattern(regexp = "^[0-9]{12}$", message = "Destination account number must be exactly 12 digits")
    private String destinationAccountNumber;

    @NotNull(message = "Gross amount cannot be null")
    @DecimalMin(value = "0.01", message = "Gross amount must be strictly greater than zero")
    private BigDecimal grossAmount;

    @DecimalMin(value = "0.00", message = "Discount cannot be negative")
    private BigDecimal discount;

    @NotNull(message = "Net amount cannot be null")
    @DecimalMin(value = "0.01", message = "Net amount must be strictly greater than zero")
    private BigDecimal netAmount;

    @NotBlank(message = "Transaction type is required")
    @Pattern(regexp = "^(TRANSFER|UTILITY_BILL|DEPOSIT)$", message = "Transaction type must be TRANSFER, UTILITY_BILL, or DEPOSIT")
    private String transactionType;

    private LocalDateTime transactionDate;

    @Pattern(regexp = "^(SUCCESS|FAILED|PENDING|COMPLIANCE_FLAGGED)$", message = "Invalid transaction status")
    private String status;

    /**
     * Converts a TransactionRecord JPA Entity to TransactionRecordDTO.
     */
    public static TransactionRecordDTO entityToDTO(TransactionRecord entity) {
        if (entity == null) {
            return null;
        }
        return TransactionRecordDTO.builder()
                .transactionId(entity.getTransactionId())
                .sourceAccountNumber(entity.getSourceAccount() != null ? entity.getSourceAccount().getAccountNumber() : null)
                .destinationAccountNumber(entity.getDestinationAccount() != null ? entity.getDestinationAccount().getAccountNumber() : null)
                .grossAmount(entity.getGrossAmount())
                .discount(entity.getDiscount())
                .netAmount(entity.getNetAmount())
                .transactionType(entity.getTransactionType())
                .transactionDate(entity.getTransactionDate())
                .status(entity.getStatus())
                .build();
    }

    /**
     * Converts a TransactionRecordDTO to TransactionRecord JPA Entity.
     */
    public static TransactionRecord dtoToEntity(TransactionRecordDTO dto) {
        if (dto == null) {
            return null;
        }

        Account sourceStub = null;
        if (dto.getSourceAccountNumber() != null) {
            sourceStub = Account.builder().accountNumber(dto.getSourceAccountNumber()).build();
        }

        Account destStub = null;
        if (dto.getDestinationAccountNumber() != null) {
            destStub = Account.builder().accountNumber(dto.getDestinationAccountNumber()).build();
        }

        return TransactionRecord.builder()
                .transactionId(dto.getTransactionId())
                .sourceAccount(sourceStub)
                .destinationAccount(destStub)
                .grossAmount(dto.getGrossAmount())
                .discount(dto.getDiscount() != null ? dto.getDiscount() : BigDecimal.ZERO)
                .netAmount(dto.getNetAmount())
                .transactionType(dto.getTransactionType())
                .transactionDate(dto.getTransactionDate())
                .status(dto.getStatus() != null ? dto.getStatus() : "SUCCESS")
                .build();
    }
}
