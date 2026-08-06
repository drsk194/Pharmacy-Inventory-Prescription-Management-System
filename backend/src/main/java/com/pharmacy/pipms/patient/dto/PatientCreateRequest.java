package com.pharmacy.pipms.patient.dto;

import com.pharmacy.pipms.patient.entity.Gender;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PatientCreateRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 150, message = "Full name must be 2-150 characters")
    private String fullName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private Gender gender;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be exactly 10 digits")
    private String phoneNumber;

    @Email(message = "Invalid email format")
    private String email;

    @Size(max = 255, message = "Address must be under 255 characters")
    private String address;

    @Size(max = 150)
    private String emergencyContactName;

    @Pattern(regexp = "^\\d{10}$", message = "Emergency contact phone must be exactly 10 digits")
    private String emergencyContactPhone;

    // Optional: link this record to an existing user account (portal login)
    private Long userId;
}