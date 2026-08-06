package com.pharmacy.pipms.patient.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class PatientMedicationResponse {
    private Long id;
    private String drugName;
    private String dosage;
    private String frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private String prescribingDoctor;
    private String notes;
    private boolean active;
}