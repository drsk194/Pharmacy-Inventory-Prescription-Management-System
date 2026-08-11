package com.pharmacy.pipms.prescription.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PrescriptionItemRequest {
    @NotNull(message = "Drug ID is required")
    private Long drugId;

    @NotNull(message = "Prescribed quantity is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Prescribed quantity must be positive")
    private BigDecimal prescribedQuantity;

    @NotBlank(message = "Dosage is required")
    @Size(max = 100)
    private String dosage;

    @NotBlank(message = "Frequency is required")
    @Size(max = 100)
    private String frequency;

    @NotBlank(message = "Duration is required")
    @Size(max = 100)
    private String duration;

    @Size(max = 500)
    private String instructions;
}