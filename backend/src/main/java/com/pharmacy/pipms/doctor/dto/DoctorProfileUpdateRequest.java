package com.pharmacy.pipms.doctor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// Deliberately excludes verified / controlledSubstanceAuthorized — those
// get their own dedicated endpoints, since granting credentials is a
// deliberate, auditable action rather than a side effect of a general edit.
@Getter
@Setter
public class DoctorProfileUpdateRequest {

    @NotBlank(message = "License number is required")
    @Size(max = 50)
    private String licenseNumber;

    @Size(max = 150)
    private String registrationCouncil;

    @Size(max = 150)
    private String specialization;

    @Size(max = 150)
    private String qualification;
}