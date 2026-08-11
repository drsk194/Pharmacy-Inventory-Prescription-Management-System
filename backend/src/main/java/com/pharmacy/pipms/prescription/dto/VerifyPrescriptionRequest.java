package com.pharmacy.pipms.prescription.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyPrescriptionRequest {
    // Required only if a hard-blocking (but overridable) warning was found
    // — allergy SEVERE/LIFE_THREATENING or interaction CONTRAINDICATED.
    private String overrideJustification;
}