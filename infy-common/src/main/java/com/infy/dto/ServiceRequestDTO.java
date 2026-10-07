package com.infy.dto;

import com.infy.entity.Customer;
import com.infy.entity.ServiceRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object: ServiceRequestDTO
 * Maps to ServiceRequest entity with strict JSR-380 bean validations.
 * Reference: spec.md (AC-REQ-01 to AC-REQ-03) & plan.md (Section 2.1.5)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequestDTO {

    private Long serviceRequestId;
    private String trackingId;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotBlank(message = "Category is mandatory")
    @Pattern(
        regexp = "^(TRANSACTION_DISPUTE|CARD_REQUEST|ACCOUNT_ISSUE|GENERAL_QUERY)$",
        message = "Category must be TRANSACTION_DISPUTE, CARD_REQUEST, ACCOUNT_ISSUE, or GENERAL_QUERY"
    )
    private String category;

    @NotBlank(message = "Description cannot be blank")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    private String description;

    @Pattern(regexp = "^(OPEN|IN_PROGRESS|RESOLVED|CLOSED)$", message = "Status must be OPEN, IN_PROGRESS, RESOLVED, or CLOSED")
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Converts a ServiceRequest JPA Entity to ServiceRequestDTO.
     */
    public static ServiceRequestDTO entityToDTO(ServiceRequest entity) {
        if (entity == null) {
            return null;
        }
        return ServiceRequestDTO.builder()
                .serviceRequestId(entity.getServiceRequestId())
                .trackingId(entity.getTrackingId())
                .customerId(entity.getCustomer() != null ? entity.getCustomer().getCustomerId() : null)
                .category(entity.getCategory())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Converts a ServiceRequestDTO to ServiceRequest JPA Entity.
     */
    public static ServiceRequest dtoToEntity(ServiceRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Customer customerStub = null;
        if (dto.getCustomerId() != null) {
            customerStub = Customer.builder().customerId(dto.getCustomerId()).build();
        }

        return ServiceRequest.builder()
                .serviceRequestId(dto.getServiceRequestId())
                .trackingId(dto.getTrackingId())
                .customer(customerStub)
                .category(dto.getCategory())
                .description(dto.getDescription())
                .status(dto.getStatus() != null ? dto.getStatus() : "OPEN")
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}
