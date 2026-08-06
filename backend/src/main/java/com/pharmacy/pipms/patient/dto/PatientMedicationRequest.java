package com.pharmacy.pipms.patient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PatientMedicationRequest {

    @NotBlank(message = "Drug name is required")
    @Size(max = 200)
    private String drugName;

    @Size(max = 100)
    private String dosage;

    @Size(max = 100)
    private String frequency;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    @Size(max = 150)
    private String prescribingDoctor;

    @Size(max = 500)
    private String notes;
}