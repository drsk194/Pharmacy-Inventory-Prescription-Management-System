package com.pharmacy.pipms.dispensing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DispensingPrepareRequest {
    @NotNull(message = "Prescription item ID is required")
    private Long prescriptionItemId;

    @NotBlank(message = "Scanned barcode is required")
    private String scannedBarcode;
}