package com.pharmacy.pipms.prescription.controller;

import com.pharmacy.pipms.common.ApiResponse;
import com.pharmacy.pipms.common.PageResponse;
import com.pharmacy.pipms.prescription.dto.*;
import com.pharmacy.pipms.prescription.entity.PrescriptionStatus;
import com.pharmacy.pipms.prescription.service.PrescriptionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
@Tag(name = "Prescriptions", description = "Prescription submission, verification workflow, and status history")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @PostMapping
    @PreAuthorize("hasAuthority('PRESCRIPTION_CREATE')")
    public ApiResponse<PrescriptionResponse> create(@Valid @RequestBody PrescriptionCreateRequest request,
                                                      Authentication authentication) {
        return ApiResponse.success("Prescription submitted", prescriptionService.create(request, authentication.getName()));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PRESCRIPTION_READ_ALL')")
    public ApiResponse<PageResponse<PrescriptionResponse>> search(
            @RequestParam(required = false) PrescriptionStatus status,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(defaultValue = "false") boolean controlledOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(prescriptionService.search(status, patientId, doctorId, controlledOnly, pageable));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('PRESCRIPTION_READ_OWN')")
    public ApiResponse<PageResponse<PrescriptionResponse>> myPrescriptions(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(prescriptionService.getMyPrescriptions(authentication.getName(), PageRequest.of(page, size)));
    }

    @GetMapping("/queue")
    @PreAuthorize("hasAuthority('PRESCRIPTION_PROCESS')")
    public ApiResponse<List<PrescriptionResponse>> queue() {
        return ApiResponse.success(prescriptionService.getQueue());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRESCRIPTION_READ_ALL') or hasAuthority('PRESCRIPTION_READ_OWN')")
    public ApiResponse<PrescriptionResponse> getById(@PathVariable Long id, Authentication authentication) {
        boolean hasFullAccess = hasAuthority(authentication, "PRESCRIPTION_READ_ALL");
        return ApiResponse.success(prescriptionService.getById(id, authentication.getName(), hasFullAccess));
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAuthority('PRESCRIPTION_READ_ALL') or hasAuthority('PRESCRIPTION_READ_OWN')")
    public ApiResponse<List<PrescriptionStatusHistoryResponse>> history(@PathVariable Long id, Authentication authentication) {
        boolean hasFullAccess = hasAuthority(authentication, "PRESCRIPTION_READ_ALL");
        prescriptionService.getById(id, authentication.getName(), hasFullAccess); // ownership check, throws if denied
        return ApiResponse.success(prescriptionService.getHistory(id));
    }

    @PutMapping("/{id}/process")
    @PreAuthorize("hasAuthority('PRESCRIPTION_PROCESS')")
    public ApiResponse<PrescriptionCheckResponse> process(@PathVariable Long id,
                                                            @Valid @RequestBody ProcessPrescriptionRequest request,
                                                            Authentication authentication) {
        return ApiResponse.success("Prescription moved to verification queue",
                prescriptionService.process(id, request, authentication.getName()));
    }

    @PutMapping("/{id}/verify")
    @PreAuthorize("hasAuthority('PRESCRIPTION_VERIFY')")
    public ApiResponse<PrescriptionCheckResponse> verify(@PathVariable Long id,
                                                           @Valid @RequestBody VerifyPrescriptionRequest request,
                                                           Authentication authentication) {
        return ApiResponse.success("Prescription verified",
                prescriptionService.verify(id, request, authentication.getName()));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('PRESCRIPTION_REJECT')")
    public ApiResponse<PrescriptionResponse> reject(@PathVariable Long id,
                                                      @Valid @RequestBody RejectPrescriptionRequest request,
                                                      Authentication authentication) {
        return ApiResponse.success("Prescription rejected",
                prescriptionService.reject(id, request, authentication.getName()));
    }

    // Deferred to Module 11 — see Assumption 1 in this module's notes.
    // POST /api/prescriptions/{id}/dispense -> added in Module 11

    private boolean hasAuthority(Authentication authentication, String authority) {
        for (GrantedAuthority granted : authentication.getAuthorities()) {
            if (granted.getAuthority().equals(authority)) return true;
        }
        return false;
    }
}