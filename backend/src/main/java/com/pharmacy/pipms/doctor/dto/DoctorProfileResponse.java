package com.pharmacy.pipms.doctor.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DoctorProfileResponse {
    private Long id;
    private Long linkedUserId;
    private String fullName;
    private String email;
    private String licenseNumber;
    private String registrationCouncil;
    private String specialization;
    private String qualification;
    private boolean verified;
    private boolean controlledSubstanceAuthorized;
    private boolean active;
}