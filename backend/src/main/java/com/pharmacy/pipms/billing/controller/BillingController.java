package com.pharmacy.pipms.billing.controller;

import com.pharmacy.pipms.billing.dto.*;
import com.pharmacy.pipms.billing.entity.BillStatus;
import com.pharmacy.pipms.billing.service.BillingService;
import com.pharmacy.pipms.common.ApiResponse;
import com.pharmacy.pipms.common.PageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
@Tag(name = "Billing", description = "Bill generation, insurance claims, outstanding balances, and cancellation")
public class BillingController {

    private final BillingService billingService;

    @PostMapping
    @PreAuthorize("hasAuthority('BILLING_CREATE')")
    public ApiResponse<BillResponse> create(@Valid @RequestBody BillCreateRequest request) {
        return ApiResponse.success("Bill generated", billingService.createBill(request));
    }

    @GetMapping("/billable-dispensing")
    @PreAuthorize("hasAuthority('BILLING_CREATE')")
    public ApiResponse<java.util.List<BillableDispensingResponse>> billableDispensing(
            @RequestParam Long patientId) {
        return ApiResponse.success(billingService.findBillableDispensing(patientId));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('BILLING_READ_ALL')")
    public ApiResponse<PageResponse<BillResponse>> search(
            @RequestParam(required = false) BillStatus status,
            @RequestParam(required = false) Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(billingService.search(status, patientId, PageRequest.of(page, size)));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('BILLING_READ_OWN')")
    public ApiResponse<PageResponse<BillResponse>> myBills(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(billingService.getMyBills(authentication.getName(), PageRequest.of(page, size)));
    }

    @GetMapping("/outstanding")
    @PreAuthorize("hasAuthority('BILLING_READ_ALL')")
    public ApiResponse<PageResponse<BillResponse>> outstanding(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(billingService.getOutstanding(PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('BILLING_READ_ALL') or hasAuthority('BILLING_READ_OWN')")
    public ApiResponse<BillResponse> getById(@PathVariable Long id, Authentication authentication) {
        boolean hasFullAccess = hasAuthority(authentication, "BILLING_READ_ALL");
        return ApiResponse.success(billingService.getById(id, authentication.getName(), hasFullAccess));
    }

    @PostMapping("/{id}/insurance-claim")
    @PreAuthorize("hasAuthority('BILLING_READ_ALL')")
    public ApiResponse<BillResponse> submitClaim(@PathVariable Long id, @Valid @RequestBody InsuranceClaimRequest request) {
        return ApiResponse.success("Insurance claim submitted", billingService.submitInsuranceClaim(id, request));
    }

    @PatchMapping("/{id}/insurance-claim/status")
    @PreAuthorize("hasRole('PHARMACIST') or hasRole('ADMIN')")
    public ApiResponse<BillResponse> updateClaimStatus(@PathVariable Long id,
                                                        @Valid @RequestBody InsuranceClaimStatusUpdateRequest request) {
        return ApiResponse.success("Insurance claim status updated", billingService.updateInsuranceClaimStatus(id, request));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('PHARMACIST') or hasRole('ADMIN')")
    public ApiResponse<BillResponse> cancel(@PathVariable Long id) {
        return ApiResponse.success("Bill cancelled", billingService.cancel(id));
    }

    private boolean hasAuthority(Authentication authentication, String authority) {
        for (GrantedAuthority granted : authentication.getAuthorities()) {
            if (granted.getAuthority().equals(authority)) return true;
        }
        return false;
    }
}