package com.infy.bank360.servicerequest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    @Pattern(regexp = "^(TRANSACTION_DISPUTE|CARD_REQUEST|ACCOUNT_ISSUE|GENERAL_QUERY)$",
            message = "Category must be TRANSACTION_DISPUTE, CARD_REQUEST, ACCOUNT_ISSUE, or GENERAL_QUERY")
    private String category;

    @NotBlank(message = "Subject cannot be blank")
    @Size(min = 5, max = 100, message = "Subject must be between 5 and 100 characters")
    private String subject;

    @NotBlank(message = "Description cannot be blank")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    private String description;

    private String status; // OPEN, IN_PROGRESS, RESOLVED, CLOSED
    private String resolutionNotes;
    private String assignedTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
