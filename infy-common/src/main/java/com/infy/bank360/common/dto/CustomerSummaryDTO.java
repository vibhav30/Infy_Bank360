package com.infy.bank360.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSummaryDTO {
    private Long customerId;
    private String fullName;
    private String email;
    private String mobileNumber;
    private String nationalId;
    private String kycStatus;
}
