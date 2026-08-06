package com.pharmacy.pipms.patient.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

// Deliberately does NOT embed allergies/medications — those are LAZY
// collections and this DTO is built outside a transaction in several call
// paths (e.g. inside a Page.map() during search). Embedding them here would
// cause LazyInitializationException and would also cause N+1 queries on
// list endpoints (Section 14 explicitly warns against this). Use the
// dedicated /allergies and /medications endpoints instead.
@Getter
@AllArgsConstructor
public class PatientResponse {
    private Long id;
    private String medicalRecordNumber;
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;
    private String phoneNumber;
    private String email;
    private String address;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private boolean active;
    private Long linkedUserId;
}