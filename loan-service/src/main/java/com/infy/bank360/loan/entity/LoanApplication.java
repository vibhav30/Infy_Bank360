package com.infy.bank360.loan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_application")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loanId;

    @Column(nullable = false)
    private Long customerId;

    @Column(length = 12)
    private String accountNumber; // Disbursal target account

    @Column(nullable = false, length = 30)
    private String loanType; // PERSONAL, HOME, VEHICLE

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal principalAmount;

    @Column(nullable = false)
    private Integer tenureMonths;

    @Column(nullable = false, precision = 5, scale = 2)
    private Double annualInterestRate;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyEmi;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyIncome;

    @Column(nullable = false, length = 30)
    private String status; // APPLIED, APPROVED, REJECTED, DISBURSED

    @Column(length = 500)
    private String rejectionRemarks;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void onPrePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "APPLIED";
        }
    }

    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
