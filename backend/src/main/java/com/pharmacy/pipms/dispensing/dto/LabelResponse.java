package com.pharmacy.pipms.dispensing.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// FR6: "patient-specific labels with drug name, dosage, instructions,
// prescriber, and dispensing pharmacist" — structured content only, no
// physical label-printer integration (see module notes, Assumption 4).
@Getter
@AllArgsConstructor
public class LabelResponse {
    private String patientName;
    private String drugGenericName;
    private String dosage;
    private String frequency;
    private String duration;
    private String instructions;
    private String prescriberName;
    private String dispensingPharmacistName;
    private BigDecimal quantityDispensed;
    private LocalDateTime dispensedAt;
}