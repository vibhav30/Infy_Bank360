package com.infy.bank360.servicerequest.dto;

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
public class ServiceRequestStatusDTO {

    @NotBlank(message = "Status cannot be blank")
    @Pattern(regexp = "^(OPEN|IN_PROGRESS|RESOLVED|CLOSED)$",
            message = "Status must be OPEN, IN_PROGRESS, RESOLVED, or CLOSED")
    private String status;

    private String resolutionNotes;
    private String assignedTo;
}
