package com.pharmacy.pipms.patient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PatientConditionRequest {
    @NotBlank(message = "Condition name is required")
    @Size(max = 200)
    private String conditionName;

    @NotNull(message = "Diagnosed date is required")
    private LocalDate diagnosedDate;

    @Size(max = 500)
    private String notes;
}