package com.pharmacy.pipms.doctor.controller;

import com.pharmacy.pipms.common.ApiResponse;
import com.pharmacy.pipms.common.PageResponse;
import com.pharmacy.pipms.doctor.dto.*;
import com.pharmacy.pipms.doctor.service.DoctorProfileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@Tag(name = "Doctors", description = "Doctor profiles, licensing, and prescribing authority")
public class DoctorController {

    private final DoctorProfileService doctorProfileService;

    @PostMapping
    @PreAuthorize("hasAuthority('DOCTOR_MANAGE')")
    public ApiResponse<DoctorProfileResponse> createProfile(@Valid @RequestBody DoctorCreateRequest request) {
        return ApiResponse.success("Doctor profile created", doctorProfileService.createProfile(request));
    }

    // page/size as plain params (not Pageable) — avoids the Swagger
    // sort=["string"] placeholder bug we hit in Module 4.
    @GetMapping
    @PreAuthorize("hasAuthority('DOCTOR_READ_ALL') or hasAuthority('DOCTOR_MANAGE')")
    public ApiResponse<PageResponse<DoctorProfileResponse>> searchProfiles(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(doctorProfileService.searchProfiles(search, pageable));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('DOCTOR_READ_OWN')")
    public ApiResponse<DoctorProfileResponse> myProfile(Authentication authentication) {
        return ApiResponse.success(doctorProfileService.getMyProfile(authentication.getName()));
    }

    @PutMapping("/me")
    @PreAuthorize("hasAuthority('DOCTOR_READ_OWN')")
    public ApiResponse<DoctorProfileResponse> updateMyProfile(Authentication authentication,
                                                                @Valid @RequestBody DoctorSelfUpdateRequest request) {
        return ApiResponse.success("Profile updated", doctorProfileService.selfUpdate(authentication.getName(), request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCTOR_READ_ALL') or hasAuthority('DOCTOR_MANAGE') or hasAuthority('DOCTOR_READ_OWN')")
    public ApiResponse<DoctorProfileResponse> getProfile(@PathVariable Long id, Authentication authentication) {
        boolean hasFullAccess = hasAnyAuthority(authentication, "DOCTOR_READ_ALL", "DOCTOR_MANAGE");
        return ApiResponse.success(doctorProfileService.getProfileById(id, authentication.getName(), hasFullAccess));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCTOR_MANAGE')")
    public ApiResponse<DoctorProfileResponse> updateProfile(@PathVariable Long id,
                                                              @Valid @RequestBody DoctorProfileUpdateRequest request) {
        return ApiResponse.success("Doctor profile updated", doctorProfileService.updateProfile(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('DOCTOR_MANAGE')")
    public ApiResponse<DoctorProfileResponse> setStatus(@PathVariable Long id, @RequestParam boolean active) {
        return ApiResponse.success(
                active ? "Doctor profile reactivated" : "Doctor profile deactivated",
                doctorProfileService.setActive(id, active));
    }

    @PatchMapping("/{id}/verify")
    @PreAuthorize("hasAuthority('DOCTOR_MANAGE')")
    public ApiResponse<DoctorProfileResponse> setVerified(@PathVariable Long id, @RequestParam boolean verified) {
        return ApiResponse.success(
                verified ? "License verified" : "Verification revoked",
                doctorProfileService.setVerified(id, verified));
    }

    @PatchMapping("/{id}/controlled-substance-authorization")
    @PreAuthorize("hasAuthority('DOCTOR_MANAGE')")
    public ApiResponse<DoctorProfileResponse> setControlledSubstanceAuthorization(
            @PathVariable Long id, @Valid @RequestBody ControlledSubstanceAuthorizationRequest request) {
        return ApiResponse.success("Controlled-substance prescribing authority updated",
                doctorProfileService.setControlledSubstanceAuthorization(id, request));
    }

    private boolean hasAnyAuthority(Authentication authentication, String... authorities) {
        for (GrantedAuthority granted : authentication.getAuthorities()) {
            for (String authority : authorities) {
                if (granted.getAuthority().equals(authority)) return true;
            }
        }
        return false;
    }
}