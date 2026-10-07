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
public class DocumentUploadDTO {

    @NotBlank(message = "Document type is mandatory (e.g. PASSPORT, DRIVING_LICENSE, NATIONAL_ID)")
    private String idDocumentType;

    @NotBlank(message = "Document URL or reference is mandatory")
    private String idDocumentUrl;
}
