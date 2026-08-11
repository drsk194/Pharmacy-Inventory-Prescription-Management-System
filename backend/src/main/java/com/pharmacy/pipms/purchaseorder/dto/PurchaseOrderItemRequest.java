package com.pharmacy.pipms.purchaseorder.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PurchaseOrderItemRequest {
    @NotNull(message = "Drug ID is required")
    private Long drugId;

    @NotNull(message = "Ordered quantity is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Ordered quantity must be positive")
    private BigDecimal orderedQuantity;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be positive")
    @Digits(integer = 8, fraction = 2, message = "Unit price allows up to 2 decimal places")
    private BigDecimal unitPrice;
}