package com.infy.bank360.customer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycVerificationDTO {

    @NotBlank(message = "Decision status is mandatory (KYC_VERIFIED or KYC_REJECTED)")
    private String status;

    private String remarks; // Mandatory when rejected
}
