package com.pharmacy.pipms.fefo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class FefoConsumeRequest {
    @NotNull(message = "Drug ID is required")
    private Long drugId;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Quantity must be positive")
    private BigDecimal quantity;

    // Optional — Module 11 will pass "PRESCRIPTION_ITEM" + the item's ID here.
    // Defaults to a generic marker when called standalone for testing.
    private String referenceType;
    private Long referenceId;
}