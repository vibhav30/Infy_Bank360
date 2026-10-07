package com.infy.bank360.transaction.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_record")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @Column(nullable = false, length = 12)
    private String sourceAccountNumber;

    @Column(length = 12)
    private String destinationAccountNumber;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal grossAmount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal discount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal netAmount;

    @Column(nullable = false, length = 30)
    private String transactionType; // TRANSFER, UTILITY_BILL, DEPOSIT

    @Column(nullable = false)
    private LocalDateTime transactionDate;

    @Column(nullable = false, length = 20)
    private String status; // SUCCESS, FAILED, PENDING, COMPLIANCE_FLAGGED

    @Column(length = 50)
    private String referenceNumber;

    @Column(length = 50)
    private String billerCategory;

    @PrePersist
    public void onPrePersist() {
        if (this.transactionDate == null) {
            this.transactionDate = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = "SUCCESS";
        }
        if (this.discount == null) {
            this.discount = BigDecimal.ZERO;
        }
    }
}
