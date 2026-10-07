package com.infy.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity: Customer (table: customer)
 * Represents retail banking customer identity, personal KYC, and onboarding profile.
 * Reference: Tablescripts.sql (lines 32-51)
 */
@Entity
@Table(
    name = "customer",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_customer_email", columnNames = "email"),
        @UniqueConstraint(name = "uq_customer_mobile_number", columnNames = "mobile_number"),
        @UniqueConstraint(name = "uq_customer_national_id", columnNames = "national_id")
    },
    indexes = {
        @Index(name = "idx_customer_kyc_status", columnList = "kyc_status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id", nullable = false, updatable = false)
    private Long customerId;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true, length = 120)
    private String email;

    @Column(name = "mobile_number", nullable = false, unique = true, length = 15)
    private String mobileNumber;

    @Column(name = "national_id", nullable = false, unique = true, length = 30)
    private String nationalId;

    @Column(name = "kyc_status", nullable = false, length = 30)
    @Builder.Default
    private String kycStatus = "PENDING_KYC";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
