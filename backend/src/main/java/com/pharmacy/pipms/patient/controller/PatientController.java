package com.pharmacy.pipms.patient.controller;

import com.pharmacy.pipms.common.ApiResponse;
import com.pharmacy.pipms.common.PageResponse;
import com.pharmacy.pipms.patient.dto.*;
import com.pharmacy.pipms.patient.service.PatientService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@Tag(name = "Patients", description = "Patient master records, allergies, and medication history")
public class PatientController {

    private final PatientService patientService;

    @PostMapping
    @PreAuthorize("hasAuthority('PATIENT_MANAGE')")
    public ApiResponse<PatientResponse> createPatient(@Valid @RequestBody PatientCreateRequest request) {
        return ApiResponse.success("Patient record created", patientService.createPatient(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PATIENT_READ_ALL')")
    public ApiResponse<PageResponse<PatientResponse>> searchPatients(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(page, size);
        return ApiResponse.success(patientService.searchPatients(search, pageable));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('PATIENT_READ_OWN')")
    public ApiResponse<PatientResponse> myRecord(Authentication authentication) {
        return ApiResponse.success(patientService.getMyPatientRecord(authentication.getName()));
    }

    @PutMapping("/me")
    @PreAuthorize("hasAuthority('PATIENT_READ_OWN')")
    public ApiResponse<PatientResponse> updateMyRecord(Authentication authentication,
                                                        @Valid @RequestBody PatientSelfUpdateRequest request) {
        return ApiResponse.success("Profile updated", patientService.selfUpdate(authentication.getName(), request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PATIENT_READ_ALL') or hasAuthority('PATIENT_READ_OWN')")
    public ApiResponse<PatientResponse> getPatient(@PathVariable Long id, Authentication authentication) {
        boolean hasFullAccess = hasAuthority(authentication, "PATIENT_READ_ALL");
        return ApiResponse.success(patientService.getPatientById(id, authentication.getName(), hasFullAccess));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PATIENT_MANAGE')")
    public ApiResponse<PatientResponse> updatePatient(@PathVariable Long id,
                                                       @Valid @RequestBody PatientUpdateRequest request) {
        return ApiResponse.success("Patient record updated", patientService.updatePatient(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('PATIENT_MANAGE')")
    public ApiResponse<PatientResponse> setStatus(@PathVariable Long id, @RequestParam boolean active) {
        return ApiResponse.success(
                active ? "Patient record reactivated" : "Patient record deactivated",
                patientService.setActive(id, active));
    }

    @PostMapping("/{id}/allergies")
    @PreAuthorize("hasAuthority('PATIENT_MANAGE')")
    public ApiResponse<PatientAllergyResponse> addAllergy(@PathVariable Long id,
                                                           @Valid @RequestBody PatientAllergyRequest request) {
        return ApiResponse.success("Allergy recorded", patientService.addAllergy(id, request));
    }

    @GetMapping("/{id}/allergies")
    @PreAuthorize("hasAuthority('PATIENT_READ_ALL') or hasAuthority('PATIENT_READ_OWN')")
    public ApiResponse<List<PatientAllergyResponse>> getAllergies(@PathVariable Long id, Authentication authentication) {
        boolean hasFullAccess = hasAuthority(authentication, "PATIENT_READ_ALL");
        patientService.getPatientById(id, authentication.getName(), hasFullAccess); // ownership check, throws if denied
        return ApiResponse.success(patientService.getAllergies(id));
    }

    @PostMapping("/{id}/medications")
    @PreAuthorize("hasAuthority('PATIENT_MANAGE')")
    public ApiResponse<PatientMedicationResponse> addMedication(@PathVariable Long id,
                                                                  @Valid @RequestBody PatientMedicationRequest request) {
        return ApiResponse.success("Medication history entry added", patientService.addMedication(id, request));
    }

    @GetMapping("/{id}/medications")
    @PreAuthorize("hasAuthority('PATIENT_READ_ALL') or hasAuthority('PATIENT_READ_OWN')")
    public ApiResponse<List<PatientMedicationResponse>> getMedications(@PathVariable Long id, Authentication authentication) {
        boolean hasFullAccess = hasAuthority(authentication, "PATIENT_READ_ALL");
        patientService.getPatientById(id, authentication.getName(), hasFullAccess);
        return ApiResponse.success(patientService.getMedications(id));
    }

    private boolean hasAuthority(Authentication authentication, String authority) {
        for (GrantedAuthority granted : authentication.getAuthorities()) {
            if (granted.getAuthority().equals(authority)) {
                return true;
            }
        }
        return false;
    }
    @PostMapping("/{id}/conditions")
    @PreAuthorize("hasAuthority('PATIENT_MANAGE')")
    public ApiResponse<com.pharmacy.pipms.patient.dto.PatientConditionResponse> addCondition(
            @PathVariable Long id, @Valid @RequestBody com.pharmacy.pipms.patient.dto.PatientConditionRequest request) {
        return ApiResponse.success("Medical condition recorded", patientService.addCondition(id, request));
    }

    @GetMapping("/{id}/conditions")
    @PreAuthorize("hasAuthority('PATIENT_READ_ALL') or hasAuthority('PATIENT_READ_OWN')")
    public ApiResponse<List<com.pharmacy.pipms.patient.dto.PatientConditionResponse>> getConditions(
            @PathVariable Long id, Authentication authentication) {
        boolean hasFullAccess = hasAuthority(authentication, "PATIENT_READ_ALL");
        patientService.getPatientById(id, authentication.getName(), hasFullAccess);
        return ApiResponse.success(patientService.getConditions(id));
    }
}