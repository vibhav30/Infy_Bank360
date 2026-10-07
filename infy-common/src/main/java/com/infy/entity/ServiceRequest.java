package com.infy.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity: ServiceRequest (table: service_request)
 * Represents customer support tickets, disputes, and service request tracking.
 * Reference: Tablescripts.sql (lines 151-175)
 */
@Entity
@Table(
    name = "service_request",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_service_request_tracking_id", columnNames = "tracking_id")
    },
    indexes = {
        @Index(name = "idx_service_request_customer_id", columnList = "customer_id"),
        @Index(name = "idx_service_request_status", columnList = "status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "customer")
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_request_id", nullable = false, updatable = false)
    private Long serviceRequestId;

    @Column(name = "tracking_id", nullable = false, unique = true, length = 30)
    private String trackingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_service_request_customer"))
    private Customer customer;

    @Column(name = "category", nullable = false, length = 50)
    private String category; // TRANSACTION_DISPUTE, CARD_REQUEST, ACCOUNT_ISSUE, GENERAL_QUERY

    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "OPEN"; // OPEN, IN_PROGRESS, RESOLVED, CLOSED

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
