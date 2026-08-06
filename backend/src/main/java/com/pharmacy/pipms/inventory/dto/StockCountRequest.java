package com.pharmacy.pipms.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class StockCountRequest {

    @NotNull(message = "Batch ID is required")
    private Long batchId;

    @NotNull(message = "Counted quantity is required")
    @DecimalMin(value = "0.0", message = "Counted quantity cannot be negative")
    private BigDecimal countedQuantity;

    @Size(max = 500)
    private String notes;
}