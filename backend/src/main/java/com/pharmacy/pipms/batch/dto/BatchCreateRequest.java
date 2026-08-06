package com.pharmacy.pipms.batch.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class BatchCreateRequest {

    @NotNull(message = "Drug ID is required")
    private Long drugId;

    @NotBlank(message = "Batch number is required")
    @Pattern(regexp = "^[A-Za-z0-9]{3,50}$", message = "Batch number must be alphanumeric, 3-50 characters")
    private String batchNumber;

    @NotNull(message = "Manufacturing date is required")
    @PastOrPresent(message = "Manufacturing date cannot be in the future")
    private LocalDate manufacturingDate;

    @NotNull(message = "Expiry date is required")
    @Future(message = "Expiry date must be in the future")
    private LocalDate expiryDate;

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    @NotNull(message = "Quantity received is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Quantity received must be positive")
    private BigDecimal quantityReceived;

    @NotNull(message = "Purchase price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Purchase price must be positive")
    @Digits(integer = 8, fraction = 2, message = "Purchase price allows up to 2 decimal places")
    private BigDecimal purchasePrice;

    @NotNull(message = "MRP is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "MRP must be positive")
    @Digits(integer = 8, fraction = 2, message = "MRP allows up to 2 decimal places")
    private BigDecimal mrp;

    @NotNull(message = "Location ID is required")
    private Long locationId;

    // Appendix F / Module 1 rule: batches with <6 months shelf life are
    // rejected unless this is provided — and only an Admin's token may
    // successfully use it (enforced in BatchService).
    private String shortShelfLifeOverrideReason;
}