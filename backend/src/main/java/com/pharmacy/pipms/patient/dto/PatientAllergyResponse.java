package com.pharmacy.pipms.patient.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class PatientAllergyResponse {
    private Long id;
    private String allergen;
    private String severity;
    private String reactionDescription;
    private LocalDate notedDate;
}