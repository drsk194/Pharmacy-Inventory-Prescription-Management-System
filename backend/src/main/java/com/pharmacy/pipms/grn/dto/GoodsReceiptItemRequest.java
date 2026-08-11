package com.pharmacy.pipms.grn.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class GoodsReceiptItemRequest {

    @NotNull(message = "Purchase order item ID is required")
    private Long purchaseOrderItemId;

    @NotBlank(message = "Batch number is required")
    @Pattern(regexp = "^[A-Za-z0-9]{3,50}$", message = "Batch number must be alphanumeric, 3-50 characters")
    private String batchNumber;

    @NotNull(message = "Manufacturing date is required")
    @PastOrPresent(message = "Manufacturing date cannot be in the future")
    private LocalDate manufacturingDate;

    @NotNull(message = "Expiry date is required")
    @Future(message = "Expiry date must be in the future")
    private LocalDate expiryDate;

    @NotNull(message = "Received quantity is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Received quantity must be positive")
    private BigDecimal receivedQuantity;

    // Optional — defaults to the PO item's agreed unit price if omitted.
    @DecimalMin(value = "0.0", inclusive = false, message = "Purchase price must be positive")
    private BigDecimal purchasePrice;

    @NotNull(message = "MRP is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "MRP must be positive")
    private BigDecimal mrp;

    @NotNull(message = "Location ID is required")
    private Long locationId;

    @Size(max = 500)
    private String conditionNotes;

    private boolean qualityDiscrepancy = false;

    @Size(max = 500)
    private String qualityNotes;

    // Passed straight through to BatchService — see Module 8's shelf-life
    // override rule (Admin-only).
    private String shortShelfLifeOverrideReason;
}