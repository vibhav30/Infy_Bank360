package com.infy.bank360.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {

    private Long customerId;

    @NotBlank(message = "Customer full name cannot be blank")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String fullName;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Mobile number is mandatory")
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Mobile number must be a valid 10-digit number starting with 6-9")
    private String mobileNumber;

    @NotBlank(message = "National ID is mandatory")
    @Pattern(regexp = "^[A-Z0-9]{8,16}$", message = "National ID must be alphanumeric and 8-16 characters")
    private String nationalId;

    private String kycStatus; // PENDING_KYC, SUBMITTED_FOR_VERIFICATION, KYC_VERIFIED, KYC_REJECTED
    private String rejectionReason;
    private String idDocumentType;
    private String idDocumentUrl;
}
