package com.pharmacy.pipms.prescription.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VerificationWarningResponse {
    private String type;      // ALLERGY, INTERACTION, DUPLICATE_THERAPY, CONTRAINDICATION, DOSAGE, DOCTOR_UNVERIFIED
    private String severity;  // INFO, WARNING, BLOCKING
    private String message;
}