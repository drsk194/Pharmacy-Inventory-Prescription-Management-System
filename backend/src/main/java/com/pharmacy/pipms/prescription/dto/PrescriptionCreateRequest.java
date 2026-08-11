package com.pharmacy.pipms.prescription.dto;

import com.pharmacy.pipms.prescription.entity.PrescriptionSource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class PrescriptionCreateRequest {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotNull(message = "Prescription date is required")
    @PastOrPresent(message = "Prescription date cannot be in the future")
    private LocalDate prescriptionDate;

    @NotNull(message = "Source is required")
    private PrescriptionSource source;

    @Size(max = 1000)
    private String notes;

    // Required only when source = ELECTRONIC — checked in the service layer.
    private String digitalSignatureReference;

    @NotEmpty(message = "At least one prescription item is required")
    @Valid
    private List<PrescriptionItemRequest> items;
}