package com.infy.bank360.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true, length = 60)
    private String username;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 30)
    private String role; // ROLE_CUSTOMER, ROLE_EMPLOYEE, ROLE_COMPLIANCE

    @Column(nullable = false, length = 30)
    private String status; // ACTIVE, PENDING_KYC, BLOCKED

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
