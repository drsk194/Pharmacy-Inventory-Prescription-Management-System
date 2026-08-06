package com.pharmacy.pipms.patient.dto;

import com.pharmacy.pipms.patient.entity.AllergySeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatientAllergyRequest {

    @NotBlank(message = "Allergen is required")
    @Size(max = 150)
    private String allergen;

    @NotNull(message = "Severity is required")
    private AllergySeverity severity;

    @Size(max = 500)
    private String reactionDescription;
}