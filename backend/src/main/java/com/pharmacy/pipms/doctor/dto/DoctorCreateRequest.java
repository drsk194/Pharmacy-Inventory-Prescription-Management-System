package com.pharmacy.pipms.doctor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DoctorCreateRequest {

    @NotNull(message = "User ID is required")
    private Long userId; // must be an existing registered user with ROLE_DOCTOR

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