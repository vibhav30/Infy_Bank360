package com.infy.bank360.servicerequest.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "service_request")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long serviceRequestId;

    @Column(nullable = false, unique = true, length = 30)
    private String trackingId;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false, length = 50)
    private String category; // TRANSACTION_DISPUTE, CARD_REQUEST, ACCOUNT_ISSUE, GENERAL_QUERY

    @Column(nullable = false, length = 100)
    private String subject;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false, length = 30)
    private String status; // OPEN, IN_PROGRESS, RESOLVED, CLOSED

    @Column(length = 1000)
    private String resolutionNotes;

    @Column(length = 60)
    private String assignedTo;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void onPrePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "OPEN";
        }
    }

    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
