package com.pharmacy.pipms.prescription.dto;

import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProcessPrescriptionRequest {

    // FR5's "patient identity verification" — technician confirms name/DOB/
    // contact match before proceeding. Modeled as an explicit confirmation
    // flag rather than an automated cross-system check (no external HIS).
    @AssertTrue(message = "Patient identity must be confirmed before processing")
    private boolean patientIdentityConfirmed;

    private String notes;
}