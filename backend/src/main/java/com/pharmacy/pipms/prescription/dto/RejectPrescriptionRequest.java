package com.pharmacy.pipms.prescription.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectPrescriptionRequest {
    @NotBlank(message = "A rejection reason is required")
    @Size(max = 500)
    private String reason;
}