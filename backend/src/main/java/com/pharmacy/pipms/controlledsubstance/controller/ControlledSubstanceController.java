package com.pharmacy.pipms.controlledsubstance.controller;

import com.pharmacy.pipms.common.ApiResponse;
import com.pharmacy.pipms.common.PageResponse;
import com.pharmacy.pipms.controlledsubstance.dto.*;
import com.pharmacy.pipms.controlledsubstance.entity.CsTransactionType;
import com.pharmacy.pipms.controlledsubstance.service.ControlledSubstanceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/controlled-substances")
@RequiredArgsConstructor
@Tag(name = "Controlled Substances", description = "Schedule H/H1/X register, dual authorization, reconciliation, and regulatory reports")
public class ControlledSubstanceController {

    private final ControlledSubstanceService controlledSubstanceService;

    @PostMapping("/reauthenticate")
    @PreAuthorize("hasAuthority('CONTROLLED_SUBSTANCE_AUTHORIZE')")
    public ApiResponse<ReauthenticateResponse> reauthenticate(@Valid @RequestBody ReauthenticateRequest request,
                                                               Authentication authentication) {
        return ApiResponse.success("Re-authentication successful",
                controlledSubstanceService.reauthenticate(authentication.getName(), request.getPin()));
    }

    @PostMapping("/transactions")
    @PreAuthorize("hasAuthority('CONTROLLED_SUBSTANCE_AUTHORIZE')")
    public ApiResponse<CsTransactionResponse> createTransaction(@Valid @RequestBody CsTransactionCreateRequest request,
                                                                 Authentication authentication) {
        return ApiResponse.success("Controlled-substance transaction recorded",
                controlledSubstanceService.createTransaction(request, authentication.getName()));
    }

    @GetMapping("/register")
    @PreAuthorize("hasAuthority('CONTROLLED_SUBSTANCE_READ')")
    public ApiResponse<PageResponse<CsRegisterEntryResponse>> getRegister(
            @RequestParam(required = false) Long drugId,
            @RequestParam(required = false) CsTransactionType transactionType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(controlledSubstanceService.getRegister(drugId, transactionType, PageRequest.of(page, size)));
    }

    @PostMapping("/reconcile")
    @PreAuthorize("hasAuthority('CONTROLLED_SUBSTANCE_AUTHORIZE')")
    public ApiResponse<ReconciliationResponse> reconcile(@Valid @RequestBody ReconcileRequest request,
                                                          Authentication authentication) {
        return ApiResponse.success("Reconciliation recorded",
                controlledSubstanceService.reconcile(request, authentication.getName()));
    }

    @PostMapping("/discrepancies")
    @PreAuthorize("hasAuthority('CONTROLLED_SUBSTANCE_AUTHORIZE')")
    public ApiResponse<ReconciliationResponse> reportDiscrepancy(@Valid @RequestBody ReconcileRequest request,
                                                                  Authentication authentication) {
        return ApiResponse.success("Discrepancy reported",
                controlledSubstanceService.reportDiscrepancy(request, authentication.getName()));
    }

    @GetMapping("/discrepancies")
    @PreAuthorize("hasAuthority('CONTROLLED_SUBSTANCE_READ')")
    public ApiResponse<List<ReconciliationResponse>> getDiscrepancies() {
        return ApiResponse.success(controlledSubstanceService.getDiscrepancies());
    }

    @GetMapping("/reports")
    @PreAuthorize("hasAuthority('CONTROLLED_SUBSTANCE_READ')")
    public ApiResponse<List<CsDrugSummaryResponse>> getReports() {
        return ApiResponse.success(controlledSubstanceService.getReports());
    }

    @GetMapping("/register/verify-integrity")
    @PreAuthorize("hasAuthority('CONTROLLED_SUBSTANCE_READ')")
    public ApiResponse<IntegrityCheckResponse> verifyIntegrity() {
        return ApiResponse.success(controlledSubstanceService.verifyIntegrity());
    }
}