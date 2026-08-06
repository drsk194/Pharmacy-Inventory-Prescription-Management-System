package com.pharmacy.pipms.inventory.dto;

import com.pharmacy.pipms.inventory.entity.AdjustmentReasonCode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AdjustmentCreateRequest {

    @NotNull(message = "Batch ID is required")
    private Long batchId;

    // Signed: positive to increase stock, negative to decrease.
    @NotNull(message = "Adjustment quantity is required")
    private BigDecimal adjustmentQuantity;

    @NotNull(message = "Reason code is required")
    private AdjustmentReasonCode reasonCode;

    @Size(max = 500)
    private String notes;
}