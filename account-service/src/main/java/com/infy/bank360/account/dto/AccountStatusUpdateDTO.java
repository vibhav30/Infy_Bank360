package com.infy.bank360.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountStatusUpdateDTO {

    @NotBlank(message = "Status cannot be blank")
    @Pattern(regexp = "^(ACTIVE|FROZEN|CLOSED)$", message = "Status must be ACTIVE, FROZEN, or CLOSED")
    private String status;

    private String remarks;
}
