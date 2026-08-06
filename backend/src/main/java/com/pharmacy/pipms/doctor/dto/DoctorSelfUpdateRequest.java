package com.pharmacy.pipms.doctor.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// Excludes licenseNumber / verified / controlledSubstance* — credential
// fields only an admin can change, same principle as
// PatientSelfUpdateRequest excluding identity fields.
@Getter
@Setter
public class DoctorSelfUpdateRequest {

    @Size(max = 150)
    private String registrationCouncil;

    @Size(max = 150)
    private String specialization;

    @Size(max = 150)
    private String qualification;
}