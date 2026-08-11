package com.pharmacy.pipms.billing.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BillCreateRequest {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotEmpty(message = "At least one dispensing record ID is required")
    private List<Long> dispensingRecordIds;

    @DecimalMin(value = "0.0", message = "Discount percent cannot be negative")
    @DecimalMax(value = "100.0", message = "Discount percent cannot exceed 100")
    private java.math.BigDecimal discountPercent;

    @Size(max = 255)
    private String discountReason;
}