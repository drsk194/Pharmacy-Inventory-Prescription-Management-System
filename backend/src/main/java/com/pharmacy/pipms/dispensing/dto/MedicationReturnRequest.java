package com.pharmacy.pipms.dispensing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MedicationReturnRequest {
    @NotNull(message = "Dispensing record ID is required")
    private Long dispensingRecordId;

    @NotNull(message = "Batch ID is required")
    private Long batchId;

    @NotNull(message = "Quantity returned is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Quantity returned must be positive")
    private BigDecimal quantityReturned;

    @NotBlank(message = "A reason is required")
    @Size(max = 500)
    private String reason;
}