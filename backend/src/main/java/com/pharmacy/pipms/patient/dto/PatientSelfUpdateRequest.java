package com.pharmacy.pipms.patient.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// Deliberately excludes fullName/dateOfBirth — identity fields should only
// be corrected by staff, not self-edited, for data-integrity reasons.
@Getter
@Setter
public class PatientSelfUpdateRequest {

    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be exactly 10 digits")
    private String phoneNumber;

    @Email(message = "Invalid email format")
    private String email;

    @Size(max = 255)
    private String address;

    @Size(max = 150)
    private String emergencyContactName;

    @Pattern(regexp = "^\\d{10}$", message = "Emergency contact phone must be exactly 10 digits")
    private String emergencyContactPhone;
}