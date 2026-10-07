package com.infy.dto;

import com.infy.entity.Customer;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object: CustomerDTO
 * Maps to Customer entity with strict JSR-380 bean validations.
 * Reference: spec.md (AC-CUST-01) & plan.md (Section 2.1.1)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {

    private Long customerId;

    @NotBlank(message = "Customer full name cannot be blank")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Please provide a valid email address")
    @Size(max = 120, message = "Email cannot exceed 120 characters")
    private String email;

    @NotBlank(message = "Mobile number is mandatory")
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Mobile number must be a valid 10-digit number starting with 6-9")
    private String mobileNumber;

    @NotBlank(message = "National ID is mandatory")
    @Pattern(regexp = "^[A-Z0-9]{8,16}$", message = "National ID must be alphanumeric and between 8 and 16 characters")
    private String nationalId;

    @Pattern(
        regexp = "^(PENDING_KYC|SUBMITTED_FOR_VERIFICATION|KYC_VERIFIED|KYC_REJECTED)$",
        message = "KYC status must be PENDING_KYC, SUBMITTED_FOR_VERIFICATION, KYC_VERIFIED, or KYC_REJECTED"
    )
    private String kycStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Converts a Customer JPA Entity to CustomerDTO.
     */
    public static CustomerDTO entityToDTO(Customer entity) {
        if (entity == null) {
            return null;
        }
        return CustomerDTO.builder()
                .customerId(entity.getCustomerId())
                .fullName(entity.getFullName())
                .email(entity.getEmail())
                .mobileNumber(entity.getMobileNumber())
                .nationalId(entity.getNationalId())
                .kycStatus(entity.getKycStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Converts a CustomerDTO to Customer JPA Entity.
     */
    public static Customer dtoToEntity(CustomerDTO dto) {
        if (dto == null) {
            return null;
        }
        return Customer.builder()
                .customerId(dto.getCustomerId())
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .mobileNumber(dto.getMobileNumber())
                .nationalId(dto.getNationalId())
                .kycStatus(dto.getKycStatus() != null ? dto.getKycStatus() : "PENDING_KYC")
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}
