package com.pharmacy.pipms.patient.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class PatientConditionResponse {
    private Long id;
    private String conditionName;
    private LocalDate diagnosedDate;
    private String notes;
    private boolean active;
}