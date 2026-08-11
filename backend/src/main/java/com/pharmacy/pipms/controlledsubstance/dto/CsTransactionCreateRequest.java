package com.pharmacy.pipms.controlledsubstance.dto;

import com.pharmacy.pipms.controlledsubstance.entity.CsTransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CsTransactionCreateRequest {

    @NotNull(message = "Drug ID is required")
    private Long drugId;

    @NotNull(message = "Transaction type is required")
    private CsTransactionType transactionType;

    // Unsigned magnitude for RECEIPT/DISPENSING/RETURN/DISPOSAL; signed
    // value allowed for ADJUSTMENT — validated in the service layer.
    @NotNull(message = "Quantity is required")
    private BigDecimal quantity;

    // Required for RECEIPT/RETURN/ADJUSTMENT/DISPOSAL (which existing batch
    // is being moved); ignored for DISPENSING, which uses FEFO across
    // batches automatically.
    private Long batchId;

    private Long prescriptionId;

    @NotNull(message = "Technician ID is required for dual authorization")
    private Long technicianId;

    @NotBlank(message = "Technician PIN is required for dual authorization")
    private String technicianPin;

    // Required for DISPOSAL only.
    private Long witnessId;

    @Size(max = 500)
    private String notes;
}