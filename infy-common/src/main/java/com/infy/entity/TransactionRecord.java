package com.infy.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity: TransactionRecord (table: transaction_record)
 * Represents double-entry ledger entries, bill payments, and promotional discount audits.
 * Reference: Tablescripts.sql (lines 84-113)
 */
@Entity
@Table(
    name = "transaction_record",
    indexes = {
        @Index(name = "idx_txn_source_account", columnList = "source_account_number"),
        @Index(name = "idx_txn_dest_account", columnList = "destination_account_number"),
        @Index(name = "idx_txn_date", columnList = "transaction_date"),
        @Index(name = "idx_txn_status", columnList = "status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"sourceAccount", "destinationAccount"})
public class TransactionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id", nullable = false, updatable = false)
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "source_account_number",
        referencedColumnName = "account_number",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_txn_source_account")
    )
    private Account sourceAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "destination_account_number",
        referencedColumnName = "account_number",
        foreignKey = @ForeignKey(name = "fk_txn_destination_account")
    )
    private Account destinationAccount;

    @Column(name = "gross_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal grossAmount;

    @Column(name = "discount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "net_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "transaction_type", nullable = false, length = 30)
    private String transactionType; // TRANSFER, UTILITY_BILL, DEPOSIT

    @CreationTimestamp
    @Column(name = "transaction_date", nullable = false, updatable = false)
    private LocalDateTime transactionDate;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "SUCCESS"; // SUCCESS, FAILED, PENDING, COMPLIANCE_FLAGGED
}
